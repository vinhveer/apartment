package com.qvinh.apartment.features.accounts.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateReq {
	@Email
	@Size(max = 320)
	private String email;
	@Size(max = 255)
	private String displayName;
	@Size(max = 255)
	private String fullName;
	@Size(max = 50)
	private String phone;
}
