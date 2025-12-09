package com.qminh.apartment.dto.property;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PropertySelectRes {
	private Long propertyId;
	private String title;
	private BigDecimal price;
	private String description;
	private Boolean isPublic;
	private Boolean isForRent;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private PropertyTypeInfo type;
	private PropertyAreaInfo area;
	private PropertySaleInfoRes saleInfo;
	private List<PropertyGalleryRes> galleries;
	private List<PropertyDetailRes> details;

	public PropertySelectRes() {}

	public Long getPropertyId() { return propertyId; }
	public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public BigDecimal getPrice() { return price; }
	public void setPrice(BigDecimal price) { this.price = price; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Boolean getIsPublic() { return isPublic; }
	public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
	public Boolean getIsForRent() { return isForRent; }
	public void setIsForRent(Boolean isForRent) { this.isForRent = isForRent; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

	public PropertyTypeInfo getType() { return type; }
	public void setType(PropertyTypeInfo type) { this.type = type; }
	public PropertyAreaInfo getArea() { return area; }
	public void setArea(PropertyAreaInfo area) { this.area = area; }
	public PropertySaleInfoRes getSaleInfo() { return saleInfo; }
	public void setSaleInfo(PropertySaleInfoRes saleInfo) { this.saleInfo = saleInfo; }
	public List<PropertyGalleryRes> getGalleries() { return galleries; }
	public void setGalleries(List<PropertyGalleryRes> galleries) { this.galleries = galleries; }
	public List<PropertyDetailRes> getDetails() { return details; }
	public void setDetails(List<PropertyDetailRes> details) { this.details = details; }
}
