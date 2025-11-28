package com.qminh.apartment.controller;

import com.qminh.apartment.dto.ApiResponse;
import com.qminh.apartment.dto.auth.AuthRes;
import com.qminh.apartment.service.IUsersService;
import com.qminh.apartment.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
public class UsersController {

	private final UserRepository userRepository;

	public UsersController(UserRepository userRepository, IUsersService usersService) {
		this.userRepository = userRepository;
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<AuthRes.UserInfo>> me(Authentication authentication) {
		var user = userRepository.findByUsername(Objects.requireNonNull(authentication.getName()))
			.orElseThrow();
		AuthRes.UserInfo info = new AuthRes.UserInfo();
		info.setId(user.getId());
		info.setUsername(user.getUsername());
		info.setRoles(List.of("ROLE_" + Objects.requireNonNull(user.getRole()).getRoleName().toUpperCase(Locale.ROOT)));
		return ResponseEntity.ok(ApiResponse.ok("Me", info));
	}

}


