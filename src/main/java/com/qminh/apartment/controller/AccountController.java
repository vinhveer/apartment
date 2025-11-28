package com.qminh.apartment.controller;

import com.qminh.apartment.dto.ApiResponse;
import com.qminh.apartment.dto.account.AccountCreateReq;
import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.dto.user.UserUpdateReq;
import com.qminh.apartment.dto.user.UserRoleUpdateReq;
import com.qminh.apartment.service.IAccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AccountController {

	private final IAccountService accountService;

	public AccountController(IAccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping("/create-employee-account")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserRes>> createEmployeeAccount(@Valid @RequestBody AccountCreateReq req) {
		// For ADMIN accounts, allow omitting fullName/phone from API and backfill sensible defaults
		if ("ADMIN".equalsIgnoreCase(req.getRoleName())) {
			if (req.getFullName() == null || req.getFullName().isBlank()) {
				String fallbackName = req.getDisplayName() != null && !req.getDisplayName().isBlank()
					? req.getDisplayName()
					: req.getUsername();
				req.setFullName(fallbackName);
			}
			if (req.getPhone() == null || req.getPhone().isBlank()) {
				req.setPhone("N/A");
			}
		}
		UserRes res = accountService.createEmployeeAccount(req);
		return ResponseEntity.ok(ApiResponse.ok("Create employee account successfully", res));
	}

	@GetMapping("/accounts/employees")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Page<UserRes>>> getEmployeeAccounts(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) String q
	) {
		Pageable pageable = PageRequest.of(page, size);
		Page<UserRes> res = accountService.searchEmployeeAccounts(q, pageable);
		var meta = new ApiResponse.Meta(page, size, res.getTotalElements());
		return ResponseEntity.ok(ApiResponse.ok("Employee account list", res, meta));
	}

	@PutMapping("/accounts/employees/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserRes>> editEmployeeAccount(
		@PathVariable Long id,
		@Valid @RequestBody UserUpdateReq req
	) {
		UserRes res = accountService.editEmployeeAccount(id, req);
		return ResponseEntity.ok(ApiResponse.ok("Update employee account successfully", res));
	}

	@DeleteMapping("/accounts/employees/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteEmployeeAccount(@PathVariable Long id) {
		accountService.deleteEmployeeAccount(id);
		return ResponseEntity.ok(ApiResponse.ok("Delete employee account successfully", null));
	}

	@PutMapping("/accounts/employees/{id}/role")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserRes>> changeRoleEmployeeAccount(
		@PathVariable Long id,
		@Valid @RequestBody UserRoleUpdateReq req
	) {
		UserRes res = accountService.changeEmployeeRole(id, req);
		return ResponseEntity.ok(ApiResponse.ok("Update employee role successfully", res));
	}
}


