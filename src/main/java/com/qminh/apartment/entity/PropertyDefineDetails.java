package com.qminh.apartment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "property_define_details")
public class PropertyDefineDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "detail_id")
	private Integer detailId;

	@Column(name = "detail_name", nullable = false, unique = true, length = 255)
	private String detailName;

	@Column(name = "is_number", nullable = false)
	private Boolean isNumber = Boolean.FALSE;

	@Column(name = "unit", length = 50)
	private String unit;

	@Column(name = "show_in_home_page", nullable = false)
	private Boolean showInHomePage = Boolean.FALSE;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}


