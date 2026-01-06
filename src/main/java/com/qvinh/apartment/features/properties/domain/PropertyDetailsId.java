package com.qvinh.apartment.features.properties.domain;

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
public class PropertyDetailsId implements Serializable {

	@Column(name = "detail_id")
	private Integer detailId;

	@Column(name = "property_id")
	private Long propertyId;
}

