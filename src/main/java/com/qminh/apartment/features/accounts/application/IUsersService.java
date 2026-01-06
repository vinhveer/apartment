package com.qminh.apartment.features.accounts.application;

import com.qminh.apartment.features.accounts.dto.user.SelfProfileUpdateReq;
import com.qminh.apartment.features.accounts.dto.user.UserRes;
import com.qminh.apartment.features.accounts.dto.user.UserRoleUpdateReq;
import org.springframework.web.multipart.MultipartFile;

public interface IUsersService {
	UserRes updateRole(long id, UserRoleUpdateReq req);
	UserRes updateAvatar(String username, MultipartFile file);
	UserRes deleteAvatar(String username);
	UserRes updateSelfProfile(String username, SelfProfileUpdateReq req);
}
