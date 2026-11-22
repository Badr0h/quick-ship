package com.quickship.serviceexpeditions.service;

import com.quickship.serviceexpeditions.dto.*;
import com.quickship.serviceexpeditions.entite.Shipment;
import com.quickship.serviceexpeditions.entite.TrackingEntry;
import com.quickship.serviceexpeditions.enums.StatutShipment;
import com.quickship.serviceexpeditions.exception.RessourceIntrouvableException;
import com.quickship.serviceexpeditions.feign.*;
import com.quickship.serviceexpeditions.mapper.ShipmentMapper;
import com.quickship.serviceexpeditions.mapper.TrackingEntryMapper;
import com.quickship.serviceexpeditions.messaging.ShipmentEventPublisher;
import com.quickship.serviceexpeditions.repository.ShipmentRepository;
import com.quickship.serviceexpeditions.repository.TrackingEntryRepository;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final TrackingEntryRepository trackingEntryRepository;
    private final ResilientRequests resilientRequests;
    private final ClientsRequest clientsRequest;
    private final ShipmentMapper shipmentMapper;
    private final TrackingEntryMapper trackingEntryMapper;
    private final ShipmentEventPublisher eventPublisher;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           TrackingEntryRepository trackingEntryRepository,
                           ResilientRequests resilientRequests,
                           ClientsRequest clientsRequest,
                           ShipmentMapper shipmentMapper,
                           TrackingEntryMapper trackingEntryMapper,
                           ShipmentEventPublisher eventPublisher) {
        this.shipmentRepository = shipmentRepository;
        this.trackingEntryRepository = trackingEntryRepository;
        this.resilientRequests = resilientRequests;
        this.clientsRequest = clientsRequest;
        this.shipmentMapper = shipmentMapper;
        this.trackingEntryMapper = trackingEntryMapper;
        this.eventPublisher = eventPublisher;
    }

    public List<Shipment> getAll() { return shipmentRepository.findAll(); }

    public Shipment getById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Shipment introuvable"));
    }

    @Transactional
    public Shipment create(CreateShipmentDto dto, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 255) {
            throw new IllegalArgumentException("Clé d’idempotence invalide");
        }
        Optional<Shipment> existing = shipmentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Opération déjà traitée");
        }

        CustomerDto customer = resilientRequests.getCustomerByIdStrict(dto.getCustomerId());
        if (!"ACTIF".equalsIgnoreCase(customer.getStatut())) {
            throw new IllegalArgumentException("Compte expéditeur suspendu");
        }

        RouteDto route = resilientRequests.getRouteByIdStrict(dto.getRouteId());
        int reservees = route.getCapaciteReservee() == null ? 0 : route.getCapaciteReservee();
        if (reservees >= route.getCapaciteColis()) {
            throw new IllegalArgumentException("Capacité de la tournée saturée");
        }

        BigDecimal montant = BigDecimal.valueOf(dto.getPoids())
                .multiply(BigDecimal.TEN)
                .add(BigDecimal.valueOf(50))
                .setScale(2, RoundingMode.HALF_UP);

        if (customer.getEncours().add(montant).compareTo(customer.getPlafondCredit()) > 0) {
            throw new IllegalArgumentException("Plafond de crédit dépassé");
        }

        Shipment shipment = new Shipment();
        shipment.setCustomerId(dto.getCustomerId());
        shipment.setDestinataireNom(dto.getDestinataireNom());
        shipment.setAdresseDestinataire(dto.getAdresseDestinataire());
        shipment.setPoids(dto.getPoids());
        shipment.setRouteId(dto.getRouteId());
        shipment.setMontantTransport(montant);
        shipment.setNumeroSuivi("QS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        shipment.setStatut(StatutShipment.EN_ATTENTE);
        shipment.setIdempotencyKey(idempotencyKey);
        shipment.setCapaciteReservee(false);
        shipment.setCapaciteLiberee(false);
        shipment.setEncoursApplique(false);
        Shipment saved = shipmentRepository.save(shipment);

        final Long shipmentId = saved.getId();
        final Long routeId = saved.getRouteId();
        final Long customerId = saved.getCustomerId();
        final String correlationId = idempotencyKey;
        afterCommit(() -> eventPublisher.publishRequested(shipmentId, routeId, customerId, correlationId));
        return saved;
    }

    @Transactional
    public Shipment depart(Long id) {
        Shipment s = getById(id);
        if (s.getStatut() != StatutShipment.EN_ATTENTE) {
            throw new IllegalArgumentException("Le colis doit être EN_ATTENTE pour partir");
        }
        if (!s.isCapaciteReservee()) {
            throw new IllegalArgumentException("Capacité non encore réservée (attendre shipment-capacity-reserved)");
        }
        resilientRequests.getRouteByIdStrict(s.getRouteId());
        s.setStatut(StatutShipment.EN_TRANSIT);
        TrackingEntry t = new TrackingEntry();
        t.setShipmentId(s.getId());
        t.setDate(LocalDateTime.now());
        t.setLocalisation("Départ entrepôt");
        t.setCommentaire("Colis en transit");
        trackingEntryRepository.save(t);
        shipmentRepository.save(s);

        final Long shipmentId = s.getId();
        final Long routeId = s.getRouteId();
        final String correlationId = s.getIdempotencyKey();
        afterCommit(() -> eventPublisher.publishInTransit(shipmentId, routeId, correlationId));
        return s;
    }

    @Transactional
    public Shipment livrer(Long id) {
        Shipment s = getById(id);
        if (s.getStatut() != StatutShipment.EN_TRANSIT) {
            throw new IllegalArgumentException("Le colis doit être EN_TRANSIT pour être livré");
        }
        s.setStatut(StatutShipment.LIVRE);
        TrackingEntry t = new TrackingEntry();
        t.setShipmentId(s.getId());
        t.setDate(LocalDateTime.now());
        t.setLocalisation(s.getAdresseDestinataire());
        t.setCommentaire("Livré avec preuve");
        trackingEntryRepository.save(t);
        shipmentRepository.save(s);

        CustomerDto customer = resilientRequests.getCustomerByIdStrict(s.getCustomerId());
        final Long shipmentId = s.getId();
        final Long customerId = s.getCustomerId();
        final Long routeId = s.getRouteId();
        final BigDecimal montant = s.getMontantTransport();
        final String email = customer.getEmail();
        final String nom = customer.getNom();
        final String numero = s.getNumeroSuivi();
        final String correlationId = s.getIdempotencyKey();

        afterCommit(() -> eventPublisher.publishDelivered(
                shipmentId, routeId, customerId, montant, email, nom, numero, correlationId));
        return s;
    }

    @Transactional
    public Shipment retourOuPerdu(Long id, StatutShipment statut) {
        if (statut != StatutShipment.RETOUR && statut != StatutShipment.PERDU) {
            throw new IllegalArgumentException("Statut final invalide");
        }
        Shipment s = getById(id);
        if (s.getStatut() != StatutShipment.EN_TRANSIT && s.getStatut() != StatutShipment.EN_ATTENTE) {
            throw new IllegalArgumentException("Transition impossible vers " + statut);
        }
        s.setStatut(statut);
        TrackingEntry t = new TrackingEntry();
        t.setShipmentId(s.getId());
        t.setDate(LocalDateTime.now());
        t.setLocalisation("Centre de tri");
        t.setCommentaire(statut == StatutShipment.RETOUR ? "Colis retourné" : "Colis déclaré perdu");
        trackingEntryRepository.save(t);
        shipmentRepository.save(s);

        CustomerDto customer = resilientRequests.getCustomerByIdStrict(s.getCustomerId());
        final Long shipmentId = s.getId();
        final Long customerId = s.getCustomerId();
        final Long routeId = s.getRouteId();
        final BigDecimal montant = s.getMontantTransport();
        final String email = customer.getEmail();
        final String nom = customer.getNom();
        final String numero = s.getNumeroSuivi();
        final String label = statut.name();
        final String correlationId = s.getIdempotencyKey();

        afterCommit(() -> eventPublisher.publishReturnedOrLost(
                shipmentId, routeId, customerId, montant, email, nom, numero, label, correlationId));
        return s;
    }

    @Transactional
    public TrackingEntry addTracking(TrackingEntryDto dto) {
        getById(dto.getShipmentId());
        TrackingEntry t = trackingEntryMapper.toEntity(dto);
        t.setId(null);
        if (t.getDate() == null) t.setDate(LocalDateTime.now());
        return trackingEntryRepository.save(t);
    }

    public List<TrackingEntry> getTracking(Long shipmentId) {
        getById(shipmentId);
        return trackingEntryRepository.findByShipmentIdOrderByDateAsc(shipmentId);
    }

    @RateLimiter(name = "consultation", fallbackMethod = "consultationLimitee")
    @Transactional(readOnly = true)
    public ShipmentFullDto getFull(Long id) {
        Shipment s = getById(id);
        ShipmentFullDto full = new ShipmentFullDto();
        full.setShipment(shipmentMapper.toDto(s));
        full.setCustomer(resilientRequests.getCustomerByIdFb(s.getCustomerId()));
        full.setRoute(resilientRequests.getRouteByIdFb(s.getRouteId()));
        full.setTracking(getTracking(id).stream().map(trackingEntryMapper::toDto).toList());
        full.setClaims(clientsRequest.getClaimsByShipment(id));
        return full;
    }

    public ShipmentFullDto consultationLimitee(Long id, RequestNotPermitted cause) {
        Shipment s = getById(id);
        ShipmentFullDto full = new ShipmentFullDto();
        full.setShipment(shipmentMapper.toDto(s));
        full.setTracking(getTracking(id).stream().map(trackingEntryMapper::toDto).toList());
        return full;
    }

    private void afterCommit(Runnable action) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { action.run(); }
        });
    }
}
