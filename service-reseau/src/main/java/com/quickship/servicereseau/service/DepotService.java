package com.quickship.servicereseau.service;
import com.quickship.servicereseau.entite.Depot;
import com.quickship.servicereseau.exception.RessourceIntrouvableException;
import com.quickship.servicereseau.repository.DepotRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class DepotService {
    private final DepotRepository depotRepository;
    public DepotService(DepotRepository depotRepository) { this.depotRepository = depotRepository; }
    public List<Depot> getAll() { return depotRepository.findAll(); }
    public Depot getById(Long id) {
        return depotRepository.findById(id).orElseThrow(() -> new RessourceIntrouvableException("Depot introuvable"));
    }
    public Depot save(Depot d) { return depotRepository.save(d); }
    public Depot update(Long id, Depot n) {
        Depot d = getById(id);
        d.setNom(n.getNom()); d.setVille(n.getVille()); d.setCapacite(n.getCapacite());
        return depotRepository.save(d);
    }
    public void delete(Long id) { depotRepository.delete(getById(id)); }
}
