package com.quickship.serviceclients.controller;

import com.quickship.serviceclients.dto.ClaimDto;
import com.quickship.serviceclients.entite.Claim;
import com.quickship.serviceclients.mapper.ClaimMapper;
import com.quickship.serviceclients.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/claims")
public class ClaimController {
    private final ClaimService claimService;
    private final ClaimMapper claimMapper;

    public ClaimController(ClaimService claimService, ClaimMapper claimMapper) {
        this.claimService = claimService;
        this.claimMapper = claimMapper;
    }

    @GetMapping
    public ResponseEntity<List<ClaimDto>> getAll() {
        return ResponseEntity.ok(claimService.getAll().stream().map(claimMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(claimMapper.toDto(claimService.getById(id)));
    }

    @GetMapping("/by-shipment/{shipmentId}")
    public ResponseEntity<List<ClaimDto>> byShipment(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(claimService.getByShipmentId(shipmentId).stream().map(claimMapper::toDto).toList());
    }

    @PostMapping("/new")
    public ResponseEntity<ClaimDto> create(@Valid @RequestBody ClaimDto dto) {
        Claim claim = claimService.create(claimMapper.toEntity(dto));
        return ResponseEntity.created(URI.create("/claims/" + claim.getId())).body(claimMapper.toDto(claim));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClaimDto> update(@PathVariable Long id, @Valid @RequestBody ClaimDto dto) {
        return ResponseEntity.ok(claimMapper.toDto(claimService.update(id, claimMapper.toEntity(dto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        claimService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
