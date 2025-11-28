package com.qminh.apartment.service;

import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.dto.user.UserRoleUpdateReq;

public interface IUsersService {
	UserRes updateRole(long id, UserRoleUpdateReq req);
}


