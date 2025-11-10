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
@Table(name = "property_details")
public class PropertyDetails {

	@EmbeddedId
	private PropertyDetailsId id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("detailId")
	@JoinColumn(name = "detail_id", nullable = false)
	private PropertyDefineDetails detail;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("propertyId")
	@JoinColumn(name = "property_id", nullable = false)
	private Property property;

	@Column(name = "value", columnDefinition = "text")
	private String value;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}


