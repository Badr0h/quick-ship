package com.quickship.servicereseau.controller;
import com.quickship.servicereseau.dto.DepotDto;
import com.quickship.servicereseau.entite.Depot;
import com.quickship.servicereseau.mapper.DepotMapper;
import com.quickship.servicereseau.service.DepotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
@RestController
@RequestMapping("/depots")
public class DepotController {
    private final DepotService depotService;
    private final DepotMapper depotMapper;
    public DepotController(DepotService depotService, DepotMapper depotMapper) {
        this.depotService = depotService; this.depotMapper = depotMapper;
    }
    @GetMapping public ResponseEntity<List<DepotDto>> getAll() {
        return ResponseEntity.ok(depotService.getAll().stream().map(depotMapper::toDto).toList());
    }
    @GetMapping("/{id}") public ResponseEntity<DepotDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(depotMapper.toDto(depotService.getById(id)));
    }
    @PostMapping("/new") public ResponseEntity<DepotDto> create(@Valid @RequestBody DepotDto dto) {
        Depot d = depotMapper.toEntity(dto); d.setId(null); d = depotService.save(d);
        return ResponseEntity.created(URI.create("/depots/" + d.getId())).body(depotMapper.toDto(d));
    }
    @PutMapping("/{id}") public ResponseEntity<DepotDto> update(@PathVariable Long id, @Valid @RequestBody DepotDto dto) {
        return ResponseEntity.ok(depotMapper.toDto(depotService.update(id, depotMapper.toEntity(dto))));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
        depotService.delete(id); return ResponseEntity.noContent().build();
    }
}
