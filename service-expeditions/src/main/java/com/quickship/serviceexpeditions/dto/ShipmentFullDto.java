package com.quickship.serviceexpeditions.dto;

import com.quickship.serviceexpeditions.feign.ClaimDto;
import com.quickship.serviceexpeditions.feign.CustomerDto;
import com.quickship.serviceexpeditions.feign.RouteDto;
import java.util.ArrayList;
import java.util.List;

public class ShipmentFullDto {
    private ShipmentDto shipment;
    private CustomerDto customer;
    private RouteDto route;
    private List<TrackingEntryDto> tracking = new ArrayList<>();
    private List<ClaimDto> claims = new ArrayList<>();

    public ShipmentDto getShipment() { return shipment; }
    public void setShipment(ShipmentDto shipment) { this.shipment = shipment; }
    public CustomerDto getCustomer() { return customer; }
    public void setCustomer(CustomerDto customer) { this.customer = customer; }
    public RouteDto getRoute() { return route; }
    public void setRoute(RouteDto route) { this.route = route; }
    public List<TrackingEntryDto> getTracking() { return tracking; }
    public void setTracking(List<TrackingEntryDto> tracking) { this.tracking = tracking; }
    public List<ClaimDto> getClaims() { return claims; }
    public void setClaims(List<ClaimDto> claims) { this.claims = claims; }
}
