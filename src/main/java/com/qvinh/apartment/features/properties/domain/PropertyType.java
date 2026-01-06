package com.qvinh.apartment.features.properties.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "property_type")
public class PropertyType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "type_id")
	private Integer typeId;

	@Column(name = "type_name", nullable = false, unique = true, length = 255)
	private String typeName;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}

