package com.quickship.serviceclients.controller;

import com.quickship.serviceclients.dto.CustomerDto;
import com.quickship.serviceclients.entite.Customer;
import com.quickship.serviceclients.mapper.CustomerMapper;
import com.quickship.serviceclients.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    public CustomerController(CustomerService customerService, CustomerMapper customerMapper) {
        this.customerService = customerService;
        this.customerMapper = customerMapper;
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAll() {
        return ResponseEntity.ok(customerService.getAll().stream().map(customerMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerMapper.toDto(customerService.getById(id)));
    }

    @PostMapping("/new")
    public ResponseEntity<CustomerDto> create(@Valid @RequestBody CustomerDto dto) {
        Customer c = customerMapper.toEntity(dto);
        c.setId(null);
        c = customerService.save(c);
        return ResponseEntity.created(URI.create("/customers/" + c.getId())).body(customerMapper.toDto(c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> update(@PathVariable Long id, @Valid @RequestBody CustomerDto dto) {
        Customer c = customerService.update(id, customerMapper.toEntity(dto));
        return ResponseEntity.ok(customerMapper.toDto(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}