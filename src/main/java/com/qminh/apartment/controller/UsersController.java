package com.qminh.apartment.controller;

import com.qminh.apartment.dto.ApiResponse;
import com.qminh.apartment.dto.auth.AuthRes;
import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.dto.user.UserUpdateReq;
import com.qminh.apartment.service.IUsersService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.qminh.apartment.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
public class UsersController {

	private final UserRepository userRepository;
	private final IUsersService usersService;

	public UsersController(UserRepository userRepository, IUsersService usersService) {
		this.userRepository = userRepository;
		this.usersService = usersService;
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

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserRes>> get(@PathVariable Long id) {
		UserRes res = usersService.get(id);
		return ResponseEntity.ok(ApiResponse.ok("User detail", res));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Page<UserRes>>> list(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		Page<UserRes> res = usersService.list(pageable);
		var meta = new ApiResponse.Meta(page, size, res.getTotalElements());
		return ResponseEntity.ok(ApiResponse.ok("User list", res, meta));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserRes>> update(
		@PathVariable Long id,
		@Valid @RequestBody UserUpdateReq req
	) {
		UserRes res = usersService.update(id, req);
		return ResponseEntity.ok(ApiResponse.ok("Update user successfully", res));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		usersService.delete(id);
		return ResponseEntity.ok(ApiResponse.ok("Delete user successfully", null));
	}
}


