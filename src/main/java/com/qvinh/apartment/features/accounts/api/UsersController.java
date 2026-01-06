package com.qvinh.apartment.features.accounts.api;

import com.qvinh.apartment.shared.api.ApiResponse;
import com.qvinh.apartment.features.accounts.dto.user.SelfProfileUpdateReq;
import com.qvinh.apartment.features.accounts.dto.user.UserRes;
import com.qvinh.apartment.features.accounts.application.IUsersService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UsersController {
	private final IUsersService usersService;

	public UsersController(IUsersService usersService) {
		this.usersService = usersService;
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserRes>> me(Authentication authentication) {
		UserRes res = usersService.getMe(authentication.getName());
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
