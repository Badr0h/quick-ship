package com.quickship.serviceexpeditions.controller;

import com.quickship.serviceexpeditions.dto.TrackingEntryDto;
import com.quickship.serviceexpeditions.mapper.TrackingEntryMapper;
import com.quickship.serviceexpeditions.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tracking")
public class TrackingController {
    private final ShipmentService shipmentService;
    private final TrackingEntryMapper trackingEntryMapper;

    public TrackingController(ShipmentService shipmentService, TrackingEntryMapper trackingEntryMapper) {
        this.shipmentService = shipmentService;
        this.trackingEntryMapper = trackingEntryMapper;
    }

    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<List<TrackingEntryDto>> byShipment(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(shipmentService.getTracking(shipmentId).stream().map(trackingEntryMapper::toDto).toList());
    }

    @PostMapping("/new")
    public ResponseEntity<TrackingEntryDto> create(@Valid @RequestBody TrackingEntryDto dto) {
        var saved = shipmentService.addTracking(dto);
        return ResponseEntity.created(URI.create("/tracking/" + saved.getId()))
                .body(trackingEntryMapper.toDto(saved));
    }
}
