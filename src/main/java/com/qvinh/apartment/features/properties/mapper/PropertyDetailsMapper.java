package com.qvinh.apartment.features.properties.mapper;

import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsRes;
import com.qvinh.apartment.features.properties.domain.PropertyDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
	componentModel = "spring",
	nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PropertyDetailsMapper {

	@Mapping(target = "propertyId", source = "property.propertyId")
	@Mapping(target = "detailId", source = "detail.detailId")
	@Mapping(target = "detailName", source = "detail.detailName")
	@Mapping(target = "isNumber", source = "detail.isNumber")
	@Mapping(target = "unit", source = "detail.unit")
	@Mapping(target = "showInHomePage", source = "detail.showInHomePage")
	@Mapping(target = "value", source = "value")
	PropertyDetailsRes toRes(PropertyDetails entity);
}

