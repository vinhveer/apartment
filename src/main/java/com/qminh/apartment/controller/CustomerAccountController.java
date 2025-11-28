package com.qminh.apartment.controller;

import com.qminh.apartment.dto.ApiResponse;
import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.dto.user.UserUpdateReq;
import com.qminh.apartment.service.ICustomerAccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CustomerAccountController {

	private final ICustomerAccountService customerAccountService;

	public CustomerAccountController(ICustomerAccountService customerAccountService) {
		this.customerAccountService = customerAccountService;
	}

	@GetMapping("/accounts/customers")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Page<UserRes>>> getCustomerAccounts(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) String q
	) {
		Pageable pageable = PageRequest.of(page, size);
		Page<UserRes> res = customerAccountService.searchCustomerAccounts(q, pageable);
		var meta = new ApiResponse.Meta(page, size, res.getTotalElements());
		return ResponseEntity.ok(ApiResponse.ok("Customer account list", res, meta));
	}

	@PutMapping("/accounts/customers/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserRes>> editCustomerAccount(
		@PathVariable Long id,
		@Valid @RequestBody UserUpdateReq req
	) {
		UserRes res = customerAccountService.editCustomerAccount(id, req);
		return ResponseEntity.ok(ApiResponse.ok("Update customer account successfully", res));
	}

	@DeleteMapping("/accounts/customers/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteCustomerAccount(@PathVariable Long id) {
		customerAccountService.deleteCustomerAccount(id);
		return ResponseEntity.ok(ApiResponse.ok("Delete customer account successfully", null));
	}
}


