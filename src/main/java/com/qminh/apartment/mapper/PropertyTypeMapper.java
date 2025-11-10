package com.qminh.apartment.mapper;

import com.qminh.apartment.dto.property_type.PropertyTypeCreateReq;
import com.qminh.apartment.dto.property_type.PropertyTypeRes;
import com.qminh.apartment.dto.property_type.PropertyTypeUpdateReq;
import com.qminh.apartment.entity.PropertyType;
import org.mapstruct.*;

@Mapper(
  componentModel = "spring",
  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PropertyTypeMapper {

  // Tạo mới: bỏ qua id & audit (DB/Audit tự set)
  @Mapping(target = "typeId",    ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  PropertyType toEntity(PropertyTypeCreateReq req);

  // Cập nhật in-place: không overwrite bằng null
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "typeId",    ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromReq(PropertyTypeUpdateReq req, @MappingTarget PropertyType target);

  PropertyTypeRes toRes(PropertyType entity);
}
