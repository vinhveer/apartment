package com.qvinh.apartment.features.auth.dto;

import java.util.List;

public class AuthRes {
	private String accessToken;
	private UserInfo user;

	public String getAccessToken() { return accessToken; }
	public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
	public UserInfo getUser() { return user; }
	public void setUser(UserInfo user) { this.user = user; }

	public static class UserInfo {
		private Long id;
		private String username;
		private List<String> roles;
		public Long getId() { return id; }
		public void setId(Long id) { this.id = id; }
		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
		public List<String> getRoles() { return roles; }
		public void setRoles(List<String> roles) { this.roles = roles; }
	}
}

