package com.qminh.apartment.features.properties.dto.property;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

	public Long getPropertyId() { return propertyId; }
	public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public BigDecimal getPrice() { return price; }
	public void setPrice(BigDecimal price) { this.price = price; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Integer getTypeId() { return typeId; }
	public void setTypeId(Integer typeId) { this.typeId = typeId; }
	public String getTypeName() { return typeName; }
	public void setTypeName(String typeName) { this.typeName = typeName; }
	public Long getSaleUserId() { return saleUserId; }
	public void setSaleUserId(Long saleUserId) { this.saleUserId = saleUserId; }
	public String getSaleDisplayName() { return saleDisplayName; }
	public void setSaleDisplayName(String saleDisplayName) { this.saleDisplayName = saleDisplayName; }
	public String getSalePhone() { return salePhone; }
	public void setSalePhone(String salePhone) { this.salePhone = salePhone; }
	public Integer getAreaId() { return areaId; }
	public void setAreaId(Integer areaId) { this.areaId = areaId; }
	public String getAreaName() { return areaName; }
	public void setAreaName(String areaName) { this.areaName = areaName; }
	public Boolean getIsPublic() { return isPublic; }
	public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
	public Boolean getIsForRent() { return isForRent; }
	public void setIsForRent(Boolean isForRent) { this.isForRent = isForRent; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
	public String getMainImageRelativePath() { return mainImageRelativePath; }
	public void setMainImageRelativePath(String mainImageRelativePath) { this.mainImageRelativePath = mainImageRelativePath; }
}

