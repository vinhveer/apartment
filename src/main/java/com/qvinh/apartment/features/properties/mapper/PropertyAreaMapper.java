package com.qvinh.apartment.features.properties.mapper;

import com.qvinh.apartment.features.properties.dto.property_area.PropertyAreaCreateReq;
import com.qvinh.apartment.features.properties.dto.property_area.PropertyAreaRes;
import com.qvinh.apartment.features.properties.dto.property_area.PropertyAreaUpdateReq;
import com.qvinh.apartment.features.properties.domain.PropertyArea;
import org.mapstruct.*;

@Mapper(
  componentModel = "spring",
  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PropertyAreaMapper {

  @Mapping(target = "areaId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  PropertyArea toEntity(PropertyAreaCreateReq req);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "areaId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromReq(PropertyAreaUpdateReq req, @MappingTarget PropertyArea entity);

  PropertyAreaRes toRes(PropertyArea entity);
}
