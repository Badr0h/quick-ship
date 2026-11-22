package com.quickship.serviceexpeditions.feign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@FeignClient(name = "service-clients")
public interface ClientsRequest {
    @GetMapping("/customers/{id}") CustomerDto getCustomerById(@PathVariable("id") Long id);
    @PostMapping("/customers/{id}/encours") CustomerDto augmenterEncours(@PathVariable("id") Long id, @RequestBody EncoursUpdateDto dto);
    @GetMapping("/claims/by-shipment/{shipmentId}") List<ClaimDto> getClaimsByShipment(@PathVariable("shipmentId") Long shipmentId);
    @PostMapping("/claims/new") ClaimDto createClaim(@RequestBody CreateClaimDto dto);
}
