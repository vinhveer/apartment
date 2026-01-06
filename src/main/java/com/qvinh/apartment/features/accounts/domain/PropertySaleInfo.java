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
@Table(name = "property_sale_info")
public class PropertySaleInfo {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "full_name", nullable = false, length = 255)
	private String fullName;

	@Column(name = "phone", nullable = false, length = 50)
	private String phone;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}

