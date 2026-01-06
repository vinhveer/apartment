package com.qvinh.apartment.features.accounts.application;

import com.qvinh.apartment.features.accounts.dto.user.SelfProfileUpdateReq;
import com.qvinh.apartment.features.accounts.dto.user.UserRes;
import org.springframework.web.multipart.MultipartFile;

public interface IUsersService {
	UserRes getMe(String username);
	UserRes updateAvatar(String username, MultipartFile file);
	UserRes deleteAvatar(String username);
	UserRes updateSelfProfile(String username, SelfProfileUpdateReq req);
}
