package com.qminh.apartment.features.accounts.mapper;

import com.qminh.apartment.features.accounts.dto.account.AccountCreateReq;
import com.qminh.apartment.features.accounts.domain.PropertySaleInfo;
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
	PropertySaleInfo toEntity(AccountCreateReq req);
}

