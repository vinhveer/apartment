package com.qvinh.apartment.features.accounts.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRes {
	private Long id;
	private String username;
	private String email;
	private String displayName;
	private String roleName;
	private String fullName;
	private String phone;
	private String avatar;
}
