package com.qminh.apartment.mapper;

import com.qminh.apartment.dto.property_area.PropertyAreaCreateReq;
import com.qminh.apartment.dto.property_area.PropertyAreaRes;
import com.qminh.apartment.dto.property_area.PropertyAreaUpdateReq;
import com.qminh.apartment.entity.PropertyArea;
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
