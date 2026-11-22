package com.quickship.serviceclients.service;

import com.quickship.serviceclients.entite.Claim;
import com.quickship.serviceclients.entite.Customer;
import com.quickship.serviceclients.exception.RessourceIntrouvableException;
import com.quickship.serviceclients.feign.ExpeditionsRequest;
import com.quickship.serviceclients.feign.ShipmentDto;
import com.quickship.serviceclients.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClaimService {
    private final ClaimRepository claimRepository;
    private final CustomerService customerService;
    private final ExpeditionsRequest expeditionsRequest;

    public ClaimService(ClaimRepository claimRepository, CustomerService customerService,
                        ExpeditionsRequest expeditionsRequest) {
        this.claimRepository = claimRepository;
        this.customerService = customerService;
        this.expeditionsRequest = expeditionsRequest;
    }

    public List<Claim> getAll() { return claimRepository.findAll(); }

    public Claim getById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Claim introuvable"));
    }

    public List<Claim> getByShipmentId(Long shipmentId) {
        return claimRepository.findByShipmentId(shipmentId);
    }

    @Transactional
    public Claim create(Claim claim) {
        Customer customer = customerService.getById(claim.getCustomerId());
        ShipmentDto shipment = expeditionsRequest.getShipmentById(claim.getShipmentId());
        if (shipment == null || shipment.getId() == null) {
            throw new IllegalArgumentException("Colis introuvable pour la réclamation");
        }
        if (!customer.getId().equals(shipment.getCustomerId())) {
            throw new IllegalArgumentException("Le colis n'appartient pas à cet expéditeur");
        }
        if (claimRepository.existsByShipmentId(claim.getShipmentId())) {
            throw new IllegalArgumentException("Une réclamation existe déjà pour ce colis");
        }
        claim.setId(null);
        claim.setMontant(shipment.getMontantTransport());
        if (claim.getStatut() == null || claim.getStatut().isBlank()) {
            claim.setStatut("OUVERTE");
        }
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim update(Long id, Claim incoming) {
        Claim claim = getById(id);
        if (incoming.getDescription() != null && !incoming.getDescription().isBlank()) {
            claim.setDescription(incoming.getDescription());
        }
        if (incoming.getStatut() != null && !incoming.getStatut().isBlank()) {
            claim.setStatut(incoming.getStatut());
        }
        return claimRepository.save(claim);
    }

    @Transactional
    public void delete(Long id) {
        claimRepository.delete(getById(id));
    }

    @Transactional
    public Claim ouvrirPourShipment(Long customerId, Long shipmentId, String description) {
        Claim claim = new Claim();
        claim.setCustomerId(customerId);
        claim.setShipmentId(shipmentId);
        claim.setDescription(description);
        return create(claim);
    }
}
