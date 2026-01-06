package com.qvinh.apartment.features.properties.dto.property;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertySearchReq {
	private String q;
	private String title;
	private String description;
	private List<Integer> typeIds;
	private List<Integer> areaIds;
	private List<Long> saleUserIds;
	private Boolean isPublic;
	private Boolean isForRent;
	private BigDecimal minPrice;
	private BigDecimal maxPrice;
	private LocalDateTime createdFrom;
	private LocalDateTime createdTo;
	private LocalDateTime updatedFrom;
	private LocalDateTime updatedTo;
	private List<PropertyDetailFilterReq> details;
}
