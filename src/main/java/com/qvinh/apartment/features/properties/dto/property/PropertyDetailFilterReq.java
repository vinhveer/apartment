package com.qvinh.apartment.features.properties.dto.property;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PropertyDetailFilterReq {
	@NotNull
	private Integer detailId;
	private BigDecimal number;
	private BigDecimal minNumber;
	private BigDecimal maxNumber;
	private String text;
}
