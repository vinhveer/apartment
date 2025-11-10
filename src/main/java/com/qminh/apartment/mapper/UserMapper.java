package com.qminh.apartment.mapper;

import com.qminh.apartment.dto.account.AdminCreateReq;
import com.qminh.apartment.dto.account.SaleCreateReq;
import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.dto.user.UserUpdateReq;
import com.qminh.apartment.entity.User;
import org.mapstruct.*;

@Mapper(
	componentModel = "spring",
	nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "emailVerifiedAt", ignore = true)
	@Mapping(target = "rememberToken", ignore = true)
	@Mapping(target = "propertySaleInfo", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	User toEntity(AdminCreateReq req);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "emailVerifiedAt", ignore = true)
	@Mapping(target = "rememberToken", ignore = true)
	@Mapping(target = "propertySaleInfo", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	User toEntity(SaleCreateReq req);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "username", ignore = true)
	@Mapping(target = "emailVerifiedAt", ignore = true)
	@Mapping(target = "rememberToken", ignore = true)
	@Mapping(target = "propertySaleInfo", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntityFromReq(UserUpdateReq req, @MappingTarget User entity);

	@Mapping(target = "roleName", source = "role.roleName")
	UserRes toRes(User entity);
}


