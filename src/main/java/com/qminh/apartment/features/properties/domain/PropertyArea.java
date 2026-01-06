package com.qminh.apartment.features.properties.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "property_area")
public class PropertyArea {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "area_id")
	private Integer areaId;

	@Column(name = "area_name", nullable = false, unique = true, length = 255)
	private String areaName;

	@Column(name = "area_link", nullable = false, unique = true, length = 255)
	private String areaLink;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}

