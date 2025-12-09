package com.qminh.apartment.mapper;

import com.qminh.apartment.dto.account.AccountCreateReq;
import com.qminh.apartment.dto.user.SelfProfileUpdateReq;
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
	@Mapping(target = "avatar", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	User toEntity(AccountCreateReq req);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "username", ignore = true)
	@Mapping(target = "emailVerifiedAt", ignore = true)
	@Mapping(target = "rememberToken", ignore = true)
	@Mapping(target = "propertySaleInfo", ignore = true)
	@Mapping(target = "avatar", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntityFromReq(UserUpdateReq req, @MappingTarget User entity);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "username", ignore = true)
	@Mapping(target = "email", ignore = true)
	@Mapping(target = "emailVerifiedAt", ignore = true)
	@Mapping(target = "rememberToken", ignore = true)
	@Mapping(target = "propertySaleInfo", ignore = true)
	@Mapping(target = "avatar", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntityFromSelf(SelfProfileUpdateReq req, @MappingTarget User entity);

	@Mapping(target = "roleName", source = "role.roleName")
	@Mapping(target = "fullName", source = "propertySaleInfo.fullName")
	@Mapping(target = "phone", source = "propertySaleInfo.phone")
	UserRes toRes(User entity);
}


