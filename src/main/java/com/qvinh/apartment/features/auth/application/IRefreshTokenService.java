package com.qvinh.apartment.features.auth.application;

import com.qvinh.apartment.features.auth.domain.RefreshToken;

import java.time.LocalDateTime;

public interface IRefreshTokenService {
	RefreshToken storeOrRotate(String username, String token, LocalDateTime expiresAt);
	boolean isValid(String token);
	RefreshToken rotate(String oldToken, String newToken, LocalDateTime newExp, String username);
	void revoke(String token);
}
