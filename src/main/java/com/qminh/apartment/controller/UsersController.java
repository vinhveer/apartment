package com.qminh.apartment.controller;

import com.qminh.apartment.dto.ApiResponse;
import com.qminh.apartment.dto.user.SelfProfileUpdateReq;
import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.mapper.UserMapper;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.service.IUsersService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
public class UsersController {
	private final UserRepository userRepository;
	private final IUsersService usersService;
	private final UserMapper userMapper;

	public UsersController(UserRepository userRepository, IUsersService usersService, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.usersService = usersService;
		this.userMapper = userMapper;
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserRes>> me(Authentication authentication) {
		var user = userRepository.findByUsername(Objects.requireNonNull(authentication.getName()))
			.orElseThrow();
		UserRes res = userMapper.toRes(user);
		return ResponseEntity.ok(ApiResponse.ok("Me", res));
	}

	@PutMapping("/me")
	public ResponseEntity<ApiResponse<UserRes>> updateMe(
		Authentication authentication,
		@Valid @RequestBody SelfProfileUpdateReq req
	) {
		UserRes res = usersService.updateSelfProfile(authentication.getName(), req);
		return ResponseEntity.ok(ApiResponse.ok("Update profile successfully", res));
	}

	@PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<UserRes>> updateAvatar(
		Authentication authentication,
		@RequestParam("file") MultipartFile file
	) {
		UserRes res = usersService.updateAvatar(authentication.getName(), file);
		return ResponseEntity.ok(ApiResponse.ok("Avatar updated successfully", res));
	}

	@DeleteMapping("/me/avatar")
	public ResponseEntity<ApiResponse<UserRes>> deleteAvatar(Authentication authentication) {
		UserRes res = usersService.deleteAvatar(authentication.getName());
		return ResponseEntity.ok(ApiResponse.ok("Avatar deleted successfully", res));
	}
}


