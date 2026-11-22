package com.quickship.servicereseau.mapper;
import com.quickship.servicereseau.dto.RouteDto;
import com.quickship.servicereseau.entite.Route;
import org.mapstruct.*;
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface RouteMapper {
    Route toEntity(RouteDto dto);
    RouteDto toDto(Route entity);
}
