package com.qvinh.apartment.features.properties.mapper;

import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsCreateReq;
import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsRes;
import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsUpdateReq;
import com.qvinh.apartment.features.properties.domain.PropertyDefineDetails;
import org.mapstruct.*;

@Mapper(
	componentModel = "spring",
	nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PropertyDefineDetailsMapper {

	@Mapping(target = "detailId", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	PropertyDefineDetails toEntity(PropertyDefineDetailsCreateReq req);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "detailId", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntityFromReq(PropertyDefineDetailsUpdateReq req, @MappingTarget PropertyDefineDetails entity);

	PropertyDefineDetailsRes toRes(PropertyDefineDetails entity);
}

