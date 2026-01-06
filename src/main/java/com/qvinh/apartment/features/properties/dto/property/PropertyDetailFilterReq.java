package com.qvinh.apartment.features.properties.dto.property;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PropertyDetailFilterReq {
	@NotNull
	private Integer detailId;
	private BigDecimal number;
	private BigDecimal minNumber;
	private BigDecimal maxNumber;
	private String text;

	public Integer getDetailId() {
		return detailId;
	}

	public void setDetailId(Integer detailId) {
		this.detailId = detailId;
	}

	public BigDecimal getNumber() {
		return number;
	}

	public void setNumber(BigDecimal number) {
		this.number = number;
	}

	public BigDecimal getMinNumber() {
		return minNumber;
	}

	public void setMinNumber(BigDecimal minNumber) {
		this.minNumber = minNumber;
	}

	public BigDecimal getMaxNumber() {
		return maxNumber;
	}

	public void setMaxNumber(BigDecimal maxNumber) {
		this.maxNumber = maxNumber;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
