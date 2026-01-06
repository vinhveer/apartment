package com.qvinh.apartment.features.properties.dto.property;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertyUpdateReq {
	@NotBlank
	@Size(max = 255)
	private String title;
	@NotNull
	@Digits(integer = 15, fraction = 2)
	private BigDecimal price;
	private String description;
	@NotNull
	private Integer typeId;
	@NotNull
	private Long saleUserId;
	@NotNull
	private Integer areaId;
	@NotNull
	private Boolean isPublic;
	@NotNull
	private Boolean isForRent;
}
