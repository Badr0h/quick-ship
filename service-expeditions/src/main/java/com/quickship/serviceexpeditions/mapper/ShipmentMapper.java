package com.quickship.serviceexpeditions.mapper;
import com.quickship.serviceexpeditions.dto.ShipmentDto;
import com.quickship.serviceexpeditions.entite.Shipment;
import org.mapstruct.*;
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShipmentMapper {
    ShipmentDto toDto(Shipment entity);
}
