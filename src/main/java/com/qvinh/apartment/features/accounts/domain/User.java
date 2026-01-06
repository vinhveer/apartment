package com.qvinh.apartment.features.accounts.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@Column(name = "display_name", length = 255)
	private String displayName;

	@Column(name = "username", nullable = false, unique = true, length = 100)
	private String username;

	@Column(name = "email", nullable = false, unique = true, length = 320)
	private String email;

	@Column(name = "email_verified_at")
	private LocalDateTime emailVerifiedAt;

	@Column(name = "password", nullable = false, length = 255)
	private String password;

	@Column(name = "remember_token", length = 100)
	private String rememberToken;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "avatar", columnDefinition = "TEXT")
	private String avatar;

	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY, optional = true)
	private PropertySaleInfo propertySaleInfo;
}

