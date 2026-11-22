package com.quickship.serviceclients.mapper;

import com.quickship.serviceclients.dto.CustomerDto;
import com.quickship.serviceclients.entite.Customer;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper {
    Customer toEntity(CustomerDto dto);
    CustomerDto toDto(Customer entity);
}
