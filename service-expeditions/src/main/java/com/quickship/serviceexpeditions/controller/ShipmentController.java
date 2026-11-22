package com.quickship.serviceexpeditions.controller;

import com.quickship.serviceexpeditions.dto.CreateShipmentDto;
import com.quickship.serviceexpeditions.dto.ShipmentDto;
import com.quickship.serviceexpeditions.dto.ShipmentFullDto;
import com.quickship.serviceexpeditions.enums.StatutShipment;
import com.quickship.serviceexpeditions.mapper.ShipmentMapper;
import com.quickship.serviceexpeditions.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {
    private final ShipmentService shipmentService;
    private final ShipmentMapper shipmentMapper;

    public ShipmentController(ShipmentService shipmentService, ShipmentMapper shipmentMapper) {
        this.shipmentService = shipmentService;
        this.shipmentMapper = shipmentMapper;
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDto>> getAll() {
        return ResponseEntity.ok(shipmentService.getAll().stream().map(shipmentMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentMapper.toDto(shipmentService.getById(id)));
    }

    @GetMapping("/{id}/full")
    public ResponseEntity<ShipmentFullDto> getFull(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getFull(id));
    }

    @PostMapping("/new")
    public ResponseEntity<ShipmentDto> create(@RequestHeader("idempotencyKey") String idempotencyKey,
                                              @Valid @RequestBody CreateShipmentDto dto) {
        var saved = shipmentService.create(dto, idempotencyKey);
        return ResponseEntity.created(URI.create("/shipments/" + saved.getId()))
                .body(shipmentMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShipmentDto> update(@PathVariable Long id, @Valid @RequestBody CreateShipmentDto dto) {
        return ResponseEntity.ok(shipmentMapper.toDto(shipmentService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/depart")
    public ResponseEntity<ShipmentDto> depart(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentMapper.toDto(shipmentService.depart(id)));
    }

    @PostMapping("/{id}/livrer")
    public ResponseEntity<ShipmentDto> livrer(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentMapper.toDto(shipmentService.livrer(id)));
    }

    @PostMapping("/{id}/retour")
    public ResponseEntity<ShipmentDto> retour(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentMapper.toDto(shipmentService.retourOuPerdu(id, StatutShipment.RETOUR)));
    }

    @PostMapping("/{id}/perdu")
    public ResponseEntity<ShipmentDto> perdu(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentMapper.toDto(shipmentService.retourOuPerdu(id, StatutShipment.PERDU)));
    }
}
