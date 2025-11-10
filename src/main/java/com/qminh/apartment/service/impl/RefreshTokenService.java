package com.qminh.apartment.service.impl;

import com.qminh.apartment.entity.RefreshToken;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.repository.RefreshTokenRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.service.IRefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class RefreshTokenService implements IRefreshTokenService {

	private final RefreshTokenRepository refreshRepo;
	private final UserRepository userRepo;

	public RefreshTokenService(RefreshTokenRepository refreshRepo, UserRepository userRepo) {
		this.refreshRepo = refreshRepo;
		this.userRepo = userRepo;
	}

	@Transactional
	public RefreshToken storeOrRotate(String username, String token, LocalDateTime expiresAt) {
		User user = userRepo.findByUsername(username)
			.orElseGet(() -> userRepo.findByEmail(username).orElseThrow(() -> new IllegalArgumentException("User not found")));
		RefreshToken rt = new RefreshToken();
		rt.setUser(Objects.requireNonNull(user, "user must not be null"));
		rt.setToken(token);
		rt.setExpiresAt(expiresAt);
		rt.setRevoked(false);
		return refreshRepo.save(rt);
	}

	@Transactional(readOnly = true)
	public boolean isValid(String token) {
		return refreshRepo.findByToken(token)
			.filter(rt -> !rt.isRevoked())
			.filter(rt -> rt.getExpiresAt() != null && rt.getExpiresAt().isAfter(LocalDateTime.now()))
			.isPresent();
	}

	@Transactional
	public RefreshToken rotate(String oldToken, String newToken, LocalDateTime newExp, String username) {
		refreshRepo.revoke(oldToken);
		return storeOrRotate(username, newToken, newExp);
	}

	@Transactional
	public void revoke(String token) {
		refreshRepo.revoke(token);
	}
}


