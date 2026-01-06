package com.qvinh.apartment.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginReq {
	@NotBlank
	private String username;
	@NotBlank
	private String password;
}
