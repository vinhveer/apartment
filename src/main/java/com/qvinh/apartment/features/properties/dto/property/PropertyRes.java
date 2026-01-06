package com.qvinh.apartment.features.properties.dto.property;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertyRes {
	private Long propertyId;
	private String title;
	private BigDecimal price;
	private String description;
	private Integer typeId;
	private String typeName;
	private Long saleUserId;
	private String saleDisplayName;
	private String salePhone;
	private Integer areaId;
	private String areaName;
	private Boolean isPublic;
	private Boolean isForRent;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String mainImageRelativePath;
}
