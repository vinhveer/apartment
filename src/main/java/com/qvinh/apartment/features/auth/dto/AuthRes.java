package com.qvinh.apartment.features.auth.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthRes {
	private String accessToken;
	private UserInfo user;

	@Getter
	@Setter
	@NoArgsConstructor
	public static class UserInfo {
		private Long id;
		private String username;
		private List<String> roles;
	}
}
