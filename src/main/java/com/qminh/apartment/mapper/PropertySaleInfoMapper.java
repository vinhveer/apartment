package com.qminh.apartment.mapper;

import com.qminh.apartment.dto.account.SaleCreateReq;
import com.qminh.apartment.entity.PropertySaleInfo;
import org.mapstruct.*;

@Mapper(
	componentModel = "spring",
	nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PropertySaleInfoMapper {
	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	PropertySaleInfo toEntity(SaleCreateReq req);
}


