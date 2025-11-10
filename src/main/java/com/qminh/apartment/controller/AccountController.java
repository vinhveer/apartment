package com.qminh.apartment.controller;

import com.qminh.apartment.dto.ApiResponse;
import com.qminh.apartment.dto.account.AdminCreateReq;
import com.qminh.apartment.dto.account.SaleCreateReq;
import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.service.IAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AccountController {

	private final IAccountService accountService;

	public AccountController(IAccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping("/create-sale")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserRes>> createSale(@Valid @RequestBody SaleCreateReq req) {
		UserRes res = accountService.createSale(req);
		return ResponseEntity.ok(ApiResponse.ok("Create sale account successfully", res));
	}

	@PostMapping("/create-admin")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserRes>> createAdmin(@Valid @RequestBody AdminCreateReq req) {
		UserRes res = accountService.createAdmin(req);
		return ResponseEntity.ok(ApiResponse.ok("Create admin account successfully", res));
	}
}


