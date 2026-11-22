package com.quickship.serviceclients.mapper;

import com.quickship.serviceclients.dto.ClaimDto;
import com.quickship.serviceclients.entite.Claim;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClaimMapper {
    Claim toEntity(ClaimDto dto);
    ClaimDto toDto(Claim entity);
}
