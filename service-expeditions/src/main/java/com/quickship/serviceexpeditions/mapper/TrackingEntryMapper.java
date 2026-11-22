package com.quickship.serviceexpeditions.mapper;
import com.quickship.serviceexpeditions.dto.TrackingEntryDto;
import com.quickship.serviceexpeditions.entite.TrackingEntry;
import org.mapstruct.*;
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TrackingEntryMapper {
    TrackingEntry toEntity(TrackingEntryDto dto);
    TrackingEntryDto toDto(TrackingEntry entity);
}
