package com.quickship.servicereseau.controller;
import com.quickship.servicereseau.dto.RouteDto;
import com.quickship.servicereseau.entite.Route;
import com.quickship.servicereseau.mapper.RouteMapper;
import com.quickship.servicereseau.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
@RestController
@RequestMapping("/routes")
public class RouteController {
    private final RouteService routeService;
    private final RouteMapper routeMapper;
    public RouteController(RouteService routeService, RouteMapper routeMapper) {
        this.routeService = routeService; this.routeMapper = routeMapper;
    }
    @GetMapping public ResponseEntity<List<RouteDto>> getAll() {
        return ResponseEntity.ok(routeService.getAll().stream().map(routeMapper::toDto).toList());
    }
    @GetMapping("/{id}") public ResponseEntity<RouteDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(routeMapper.toDto(routeService.getById(id)));
    }
    @PostMapping("/new") public ResponseEntity<RouteDto> create(@Valid @RequestBody RouteDto dto) {
        Route r = routeMapper.toEntity(dto); r.setId(null); r = routeService.save(r);
        return ResponseEntity.created(URI.create("/routes/" + r.getId())).body(routeMapper.toDto(r));
    }
    @PutMapping("/{id}") public ResponseEntity<RouteDto> update(@PathVariable Long id, @Valid @RequestBody RouteDto dto) {
        return ResponseEntity.ok(routeMapper.toDto(routeService.update(id, routeMapper.toEntity(dto))));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.delete(id); return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/reserver") public ResponseEntity<RouteDto> reserver(@PathVariable Long id) {
        return ResponseEntity.ok(routeMapper.toDto(routeService.reserverCapacite(id)));
    }
    @PostMapping("/{id}/liberer") public ResponseEntity<RouteDto> liberer(@PathVariable Long id) {
        return ResponseEntity.ok(routeMapper.toDto(routeService.libererCapacite(id)));
    }
}
