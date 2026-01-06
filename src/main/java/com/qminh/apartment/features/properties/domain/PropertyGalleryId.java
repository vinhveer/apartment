package com.qminh.apartment.features.properties.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class PropertyGalleryId implements Serializable {

	@Column(name = "property_id")
	private Long propertyId;

	@Column(name = "file_id")
	private Long fileId;
}

