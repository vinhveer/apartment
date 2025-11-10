package com.qminh.apartment.service;

import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.dto.user.UserUpdateReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUsersService {
	UserRes get(long id);
	Page<UserRes> list(Pageable pageable);
	UserRes update(long id, UserUpdateReq req);
	void delete(long id);
}


