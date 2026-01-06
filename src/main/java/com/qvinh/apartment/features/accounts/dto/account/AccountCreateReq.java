package com.qvinh.apartment.features.accounts.dto.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AccountCreateReq {
	@NotBlank
	@Size(max = 100)
	private String username;
	@NotBlank
	@Email
	@Size(max = 320)
	private String email;
	@NotBlank
	@Size(min = 6, max = 100)
	private String password;
	@Size(max = 255)
	private String displayName;

	// Required when roleName == SALE
	@Size(max = 255)
	private String fullName;
	@Size(max = 50)
	private String phone;

	@NotBlank
	@Pattern(regexp = "^(ADMIN|SALE)$")
	private String roleName;

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	public String getDisplayName() { return displayName; }
	public void setDisplayName(String displayName) { this.displayName = displayName; }
	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getRoleName() { return roleName; }
	public void setRoleName(String roleName) { this.roleName = roleName; }
}

