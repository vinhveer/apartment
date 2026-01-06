package com.qvinh.apartment.features.accounts.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SelfProfileUpdateReq {
	@Size(max = 255)
	private String displayName;
	@Size(max = 255)
	private String fullName;
	@Size(max = 50)
	private String phone;
}
