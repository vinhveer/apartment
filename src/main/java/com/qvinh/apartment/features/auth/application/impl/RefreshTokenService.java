package com.qvinh.apartment.features.auth.application.impl;

import com.qvinh.apartment.features.auth.application.IRefreshTokenService;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import com.qvinh.apartment.features.auth.domain.RefreshToken;
import com.qvinh.apartment.features.auth.persistence.RefreshTokenRepository;
import com.qvinh.apartment.shared.constants.ErrorMessages;
import com.qvinh.apartment.shared.error.ErrorCode;
import com.qvinh.apartment.shared.exception.AppException;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class RefreshTokenService implements IRefreshTokenService {

	private final RefreshTokenRepository refreshRepo;
	private final UserRepository userRepo;
	private final IRefreshTokenService self;

	public RefreshTokenService(RefreshTokenRepository refreshRepo, UserRepository userRepo, @Lazy IRefreshTokenService self) {
		this.refreshRepo = refreshRepo;
		this.userRepo = userRepo;
		this.self = self;
	}

	@Transactional
	public RefreshToken storeOrRotate(String username, String token, LocalDateTime expiresAt) {
		User user = userRepo.findByUsername(Objects.requireNonNull(username))
			.orElseGet(() -> userRepo.findByEmail(Objects.requireNonNull(username))
				.orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, ErrorMessages.AUTHENTICATION_REQUIRED)));
		// enforce single active refresh token per user by revoking existing ones
		refreshRepo.revokeByUserId(Objects.requireNonNull(user.getId(), "user.id must not be null"));
		RefreshToken rt = new RefreshToken();
		rt.setUser(Objects.requireNonNull(user, "user must not be null"));
		rt.setToken(token);
		rt.setExpiresAt(expiresAt);
		rt.setRevoked(false);
		return refreshRepo.save(rt);
	}

	@Transactional(readOnly = true)
	public boolean isValid(String token) {
		return refreshRepo.findByToken(Objects.requireNonNull(token))
			.filter(rt -> !rt.isRevoked())
			.filter(rt -> rt.getExpiresAt() != null && rt.getExpiresAt().isAfter(LocalDateTime.now()))
			.isPresent();
	}

	@Transactional
	public RefreshToken rotate(String oldToken, String newToken, LocalDateTime newExp, String username) {
		User user = userRepo.findByUsername(Objects.requireNonNull(username))
			.orElseGet(() -> userRepo.findByEmail(Objects.requireNonNull(username))
				.orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, ErrorMessages.AUTHENTICATION_REQUIRED)));
		RefreshToken existing = refreshRepo.findByToken(Objects.requireNonNull(oldToken))
			.orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
		// ownership and validity checks
		if (!Objects.equals(existing.getUser().getId(), user.getId())) {
			throw new AppException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid refresh token");
		}
		if (existing.isRevoked() || existing.getExpiresAt() == null || !existing.getExpiresAt().isAfter(LocalDateTime.now())) {
			throw new AppException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Invalid refresh token");
		}
		refreshRepo.revoke(Objects.requireNonNull(oldToken));
		return self.storeOrRotate(username, newToken, newExp);
	}

	@Transactional
	public void revoke(String token) {
		refreshRepo.revoke(Objects.requireNonNull(token));
	}
}
