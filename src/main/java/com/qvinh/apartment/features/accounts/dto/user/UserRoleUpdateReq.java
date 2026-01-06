package com.qvinh.apartment.features.accounts.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRoleUpdateReq {
	@NotBlank
	@Pattern(regexp = "^(ADMIN|SALE)$")
	private String roleName;

	// Required when switching to SALE
	@Size(max = 255)
	private String fullName;
	@Size(max = 50)
	private String phone;
}
