package com.quickship.serviceexpeditions.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/** Lectures synchrones uniquement (fiche V7 : encours / claim via Kafka). */
@FeignClient(name = "service-clients")
public interface ClientsRequest {
    @GetMapping("/customers/{id}")
    CustomerDto getCustomerById(@PathVariable("id") Long id);

    @GetMapping("/claims/by-shipment/{shipmentId}")
    List<ClaimDto> getClaimsByShipment(@PathVariable("shipmentId") Long shipmentId);
}
