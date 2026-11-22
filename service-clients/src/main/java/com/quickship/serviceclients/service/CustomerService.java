package com.quickship.serviceclients.service;

import com.quickship.serviceclients.entite.Customer;
import com.quickship.serviceclients.exception.RessourceIntrouvableException;
import com.quickship.serviceclients.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAll() { return customerRepository.findAll(); }

    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Customer introuvable"));
    }

    public Customer save(Customer customer) { return customerRepository.save(customer); }

    public Customer update(Long id, Customer nouveau) {
        Customer c = getById(id);
        c.setNom(nouveau.getNom());
        c.setEmail(nouveau.getEmail());
        c.setEncours(nouveau.getEncours());
        c.setPlafondCredit(nouveau.getPlafondCredit());
        c.setStatut(nouveau.getStatut());
        return customerRepository.save(c);
    }

    public void delete(Long id) { customerRepository.delete(getById(id)); }

    @Transactional
    public Customer augmenterEncours(Long id, BigDecimal montant) {
        Customer c = getById(id);
        c.setEncours(c.getEncours().add(montant));
        return customerRepository.save(c);
    }
}
