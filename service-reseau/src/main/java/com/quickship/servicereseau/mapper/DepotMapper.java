package com.quickship.servicereseau.mapper;
import com.quickship.servicereseau.dto.DepotDto;
import com.quickship.servicereseau.entite.Depot;
import org.mapstruct.*;
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepotMapper {
    Depot toEntity(DepotDto dto);
    DepotDto toDto(Depot entity);
}
