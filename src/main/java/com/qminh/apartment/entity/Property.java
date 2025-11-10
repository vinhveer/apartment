package com.qminh.apartment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "property")
public class Property {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "property_id")
	private Long propertyId;

	@Column(name = "title", nullable = false, length = 255)
	private String title;

	@Column(name = "price", nullable = false, precision = 15, scale = 2)
	private BigDecimal price;

	@Column(name = "description", columnDefinition = "text")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "type_id", nullable = false)
	private PropertyType type;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sale_id", referencedColumnName = "user_id", nullable = false)
	private PropertySaleInfo saleInfo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "area_id", nullable = false)
	private PropertyArea area;

	@Column(name = "is_public", nullable = false)
	private Boolean isPublic = Boolean.FALSE;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "property", fetch = FetchType.LAZY)
	private Set<PropertyGallery> galleries = new LinkedHashSet<>();

	@OneToMany(mappedBy = "property", fetch = FetchType.LAZY)
	private Set<PropertyDetails> details = new LinkedHashSet<>();
}


