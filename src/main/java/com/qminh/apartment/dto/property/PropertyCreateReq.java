package com.qminh.apartment.dto.property;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PropertyCreateReq {
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

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public BigDecimal getPrice() { return price; }
	public void setPrice(BigDecimal price) { this.price = price; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Integer getTypeId() { return typeId; }
	public void setTypeId(Integer typeId) { this.typeId = typeId; }
	public Long getSaleUserId() { return saleUserId; }
	public void setSaleUserId(Long saleUserId) { this.saleUserId = saleUserId; }
	public Integer getAreaId() { return areaId; }
	public void setAreaId(Integer areaId) { this.areaId = areaId; }
	public Boolean getIsPublic() { return isPublic; }
	public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
	public Boolean getIsForRent() { return isForRent; }
	public void setIsForRent(Boolean isForRent) { this.isForRent = isForRent; }
}


