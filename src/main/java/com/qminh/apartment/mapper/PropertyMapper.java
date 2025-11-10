package com.qminh.apartment.mapper;

import com.qminh.apartment.dto.property.PropertyCreateReq;
import com.qminh.apartment.dto.property.PropertyRes;
import com.qminh.apartment.dto.property.PropertyUpdateReq;
import com.qminh.apartment.entity.Property;
import org.mapstruct.*;

@Mapper(
	componentModel = "spring",
	nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PropertyMapper {

	@Mapping(target = "propertyId", ignore = true)
	@Mapping(target = "type", ignore = true)
	@Mapping(target = "saleInfo", ignore = true)
	@Mapping(target = "area", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "galleries", ignore = true)
	@Mapping(target = "details", ignore = true)
	Property toEntity(PropertyCreateReq req);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "propertyId", ignore = true)
	@Mapping(target = "type", ignore = true)
	@Mapping(target = "saleInfo", ignore = true)
	@Mapping(target = "area", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "galleries", ignore = true)
	@Mapping(target = "details", ignore = true)
	void updateEntityFromReq(PropertyUpdateReq req, @MappingTarget Property entity);

	@Mapping(target = "typeId", source = "type.typeId")
	@Mapping(target = "saleUserId", source = "saleInfo.userId")
	@Mapping(target = "areaId", source = "area.areaId")
	PropertyRes toRes(Property entity);
}


