package com.qminh.apartment.dto.user;

public class UserRes {
	private Long id;
	private String username;
	private String email;
	private String displayName;
	private String roleName;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getDisplayName() { return displayName; }
	public void setDisplayName(String displayName) { this.displayName = displayName; }
	public String getRoleName() { return roleName; }
	public void setRoleName(String roleName) { this.roleName = roleName; }
}


