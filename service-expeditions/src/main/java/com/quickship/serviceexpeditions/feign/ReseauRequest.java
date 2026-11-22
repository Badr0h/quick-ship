package com.quickship.serviceexpeditions.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Lectures synchrones uniquement (fiche V7 : écritures capacité via Kafka). */
@FeignClient(name = "service-reseau")
public interface ReseauRequest {
    @GetMapping("/routes/{id}")
    RouteDto getRouteById(@PathVariable("id") Long id);
}
