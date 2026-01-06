package com.qvinh.apartment.features.accounts.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRoleUpdateReq {
	@NotBlank
	@Pattern(regexp = "^(ADMIN|SALE)$")
	private String roleName;

	// Required when switching to SALE
	@Size(max = 255)
	private String fullName;
	@Size(max = 50)
	private String phone;

	public String getRoleName() { return roleName; }
	public void setRoleName(String roleName) { this.roleName = roleName; }
	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
}

