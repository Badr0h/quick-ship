package com.quickship.serviceexpeditions.service;

import com.quickship.serviceexpeditions.dto.*;
import com.quickship.serviceexpeditions.entite.Shipment;
import com.quickship.serviceexpeditions.entite.TrackingEntry;
import com.quickship.serviceexpeditions.enums.StatutShipment;
import com.quickship.serviceexpeditions.exception.RessourceIntrouvableException;
import com.quickship.serviceexpeditions.feign.*;
import com.quickship.serviceexpeditions.mapper.ShipmentMapper;
import com.quickship.serviceexpeditions.mapper.TrackingEntryMapper;
import com.quickship.serviceexpeditions.messaging.NotificationPublisher;
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
    private final ReseauRequest reseauRequest;
    private final ShipmentMapper shipmentMapper;
    private final TrackingEntryMapper trackingEntryMapper;
    private final NotificationPublisher notificationPublisher;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           TrackingEntryRepository trackingEntryRepository,
                           ResilientRequests resilientRequests,
                           ClientsRequest clientsRequest,
                           ReseauRequest reseauRequest,
                           ShipmentMapper shipmentMapper,
                           TrackingEntryMapper trackingEntryMapper,
                           NotificationPublisher notificationPublisher) {
        this.shipmentRepository = shipmentRepository;
        this.trackingEntryRepository = trackingEntryRepository;
        this.resilientRequests = resilientRequests;
        this.clientsRequest = clientsRequest;
        this.reseauRequest = reseauRequest;
        this.shipmentMapper = shipmentMapper;
        this.trackingEntryMapper = trackingEntryMapper;
        this.notificationPublisher = notificationPublisher;
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

        reseauRequest.reserverCapacite(dto.getRouteId());

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
        shipment.setCapaciteReservee(true);
        shipment.setCapaciteLiberee(false);
        shipment.setEncoursApplique(false);
        return shipmentRepository.save(shipment);
    }

    @Transactional
    public Shipment depart(Long id) {
        Shipment s = getById(id);
        if (s.getStatut() != StatutShipment.EN_ATTENTE) {
            throw new IllegalArgumentException("Le colis doit être EN_ATTENTE pour partir");
        }
        // validation sans second décompte de capacité
        resilientRequests.getRouteByIdStrict(s.getRouteId());
        s.setStatut(StatutShipment.EN_TRANSIT);
        TrackingEntry t = new TrackingEntry();
        t.setShipmentId(s.getId());
        t.setDate(LocalDateTime.now());
        t.setLocalisation("Départ entrepôt");
        t.setCommentaire("Colis en transit");
        trackingEntryRepository.save(t);
        return shipmentRepository.save(s);
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

        afterCommit(() -> {
            libererCapaciteUneFois(shipmentId, routeId);
            appliquerEncoursUneFois(shipmentId, customerId, montant);
            notificationPublisher.envoyer(new NotificationEmailDto(
                    email,
                    "Colis livré " + numero,
                    "Bonjour " + nom + ", votre colis " + numero + " a été livré."
            ));
        });
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
        final String email = customer.getEmail();
        final String nom = customer.getNom();
        final String numero = s.getNumeroSuivi();
        final String label = statut.name();

        afterCommit(() -> {
            libererCapaciteUneFois(shipmentId, routeId);
            clientsRequest.createClaim(new CreateClaimDto(
                    customerId, shipmentId, "Réclamation automatique : colis " + label
            ));
            notificationPublisher.envoyer(new NotificationEmailDto(
                    email,
                    "Colis " + label + " " + numero,
                    "Bonjour " + nom + ", votre colis " + numero + " est passé en statut " + label + "."
            ));
        });
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

    private void libererCapaciteUneFois(Long shipmentId, Long routeId) {
        Shipment s = getById(shipmentId);
        if (s.isCapaciteLiberee() || !s.isCapaciteReservee()) return;
        reseauRequest.libererCapacite(routeId);
        s.setCapaciteLiberee(true);
        shipmentRepository.save(s);
    }

    private void appliquerEncoursUneFois(Long shipmentId, Long customerId, BigDecimal montant) {
        Shipment s = getById(shipmentId);
        if (s.isEncoursApplique()) return;
        clientsRequest.augmenterEncours(customerId, new EncoursUpdateDto(montant));
        s.setEncoursApplique(true);
        shipmentRepository.save(s);
    }

    private void afterCommit(Runnable action) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { action.run(); }
        });
    }
}
