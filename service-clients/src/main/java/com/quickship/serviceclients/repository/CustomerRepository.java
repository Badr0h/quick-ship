package com.quickship.serviceclients.repository;

import com.quickship.serviceclients.entite.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {}
