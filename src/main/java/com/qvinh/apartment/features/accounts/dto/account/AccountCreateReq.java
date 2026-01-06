package com.qvinh.apartment.features.accounts.dto.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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
}
