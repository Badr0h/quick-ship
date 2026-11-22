package com.quickship.serviceexpeditions.feign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name = "service-reseau")
public interface ReseauRequest {
    @GetMapping("/routes/{id}") RouteDto getRouteById(@PathVariable("id") Long id);
    @PostMapping("/routes/{id}/reserver") RouteDto reserverCapacite(@PathVariable("id") Long id);
    @PostMapping("/routes/{id}/liberer") RouteDto libererCapacite(@PathVariable("id") Long id);
}
