package com.quickship.servicereseau.service;
import com.quickship.servicereseau.entite.Route;
import com.quickship.servicereseau.exception.RessourceIntrouvableException;
import com.quickship.servicereseau.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class RouteService {
    private final RouteRepository routeRepository;
    public RouteService(RouteRepository routeRepository) { this.routeRepository = routeRepository; }
    public List<Route> getAll() { return routeRepository.findAll(); }
    public Route getById(Long id) {
        return routeRepository.findById(id).orElseThrow(() -> new RessourceIntrouvableException("Route introuvable"));
    }
    public Route save(Route r) {
        if (r.getCapaciteReservee() == null) r.setCapaciteReservee(0);
        return routeRepository.save(r);
    }
    public Route update(Long id, Route n) {
        Route r = getById(id);
        r.setNom(n.getNom()); r.setVilleDepart(n.getVilleDepart()); r.setVilleArrivee(n.getVilleArrivee());
        r.setDateDepart(n.getDateDepart()); r.setDureeEstimee(n.getDureeEstimee());
        r.setCapaciteColis(n.getCapaciteColis());
        return routeRepository.save(r);
    }
    public void delete(Long id) { routeRepository.delete(getById(id)); }

    @Transactional
    public Route reserverCapacite(Long id) {
        Route r = getById(id);
        if (r.getCapaciteReservee() >= r.getCapaciteColis()) {
            throw new IllegalArgumentException("Capacité de la tournée saturée");
        }
        r.setCapaciteReservee(r.getCapaciteReservee() + 1);
        return routeRepository.save(r);
    }

    @Transactional
    public Route libererCapacite(Long id) {
        Route r = getById(id);
        if (r.getCapaciteReservee() <= 0) {
            return r;
        }
        r.setCapaciteReservee(r.getCapaciteReservee() - 1);
        return routeRepository.save(r);
    }
}
