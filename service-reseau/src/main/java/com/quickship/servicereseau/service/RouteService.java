package com.quickship.servicereseau.service;

import com.quickship.servicereseau.entite.CapaciteAffectation;
import com.quickship.servicereseau.entite.Route;
import com.quickship.servicereseau.exception.RessourceIntrouvableException;
import com.quickship.servicereseau.repository.CapaciteAffectationRepository;
import com.quickship.servicereseau.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RouteService {
    private final RouteRepository routeRepository;
    private final CapaciteAffectationRepository capaciteAffectationRepository;

    public RouteService(RouteRepository routeRepository,
                        CapaciteAffectationRepository capaciteAffectationRepository) {
        this.routeRepository = routeRepository;
        this.capaciteAffectationRepository = capaciteAffectationRepository;
    }

    public List<Route> getAll() { return routeRepository.findAll(); }

    public Route getById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Route introuvable"));
    }

    public Route save(Route r) {
        if (r.getCapaciteReservee() == null) r.setCapaciteReservee(0);
        return routeRepository.save(r);
    }

    public Route update(Long id, Route n) {
        Route r = getById(id);
        r.setNom(n.getNom());
        r.setVilleDepart(n.getVilleDepart());
        r.setVilleArrivee(n.getVilleArrivee());
        r.setDateDepart(n.getDateDepart());
        r.setDureeEstimee(n.getDureeEstimee());
        r.setCapaciteColis(n.getCapaciteColis());
        return routeRepository.save(r);
    }

    public void delete(Long id) { routeRepository.delete(getById(id)); }

    /**
     * Réserve atomiquement 1 unité pour un colis (verrou pessimiste + affectation unique).
     */
    @Transactional
    public Route reserverCapacitePourShipment(Long routeId, Long shipmentId) {
        if (capaciteAffectationRepository.existsByShipmentId(shipmentId)) {
            return getById(routeId);
        }
        Route r = routeRepository.findByIdForUpdate(routeId)
                .orElseThrow(() -> new RessourceIntrouvableException("Route introuvable"));
        if (r.getCapaciteReservee() >= r.getCapaciteColis()) {
            throw new IllegalArgumentException("Capacité de la tournée saturée");
        }
        r.setCapaciteReservee(r.getCapaciteReservee() + 1);
        routeRepository.save(r);
        capaciteAffectationRepository.save(new CapaciteAffectation(shipmentId, routeId));
        return r;
    }

    /**
     * Libère 1 unité uniquement si le colis avait réservé et que la capacité n'a pas déjà été libérée.
     * @return true si une unité a effectivement été libérée
     */
    @Transactional
    public boolean libererCapacitePourShipment(Long shipmentId) {
        return capaciteAffectationRepository.findByShipmentId(shipmentId).map(aff -> {
            if (aff.isLiberee()) {
                return false;
            }
            Route r = routeRepository.findByIdForUpdate(aff.getRouteId())
                    .orElseThrow(() -> new RessourceIntrouvableException("Route introuvable"));
            if (r.getCapaciteReservee() > 0) {
                r.setCapaciteReservee(r.getCapaciteReservee() - 1);
                routeRepository.save(r);
            }
            aff.setLiberee(true);
            capaciteAffectationRepository.save(aff);
            return true;
        }).orElse(false);
    }
}
