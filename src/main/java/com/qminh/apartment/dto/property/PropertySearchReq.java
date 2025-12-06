package com.qminh.apartment.dto.property;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Integer> getTypeIds() {
		return typeIds;
	}

	public void setTypeIds(List<Integer> typeIds) {
		this.typeIds = typeIds;
	}

	public List<Integer> getAreaIds() {
		return areaIds;
	}

	public void setAreaIds(List<Integer> areaIds) {
		this.areaIds = areaIds;
	}

	public List<Long> getSaleUserIds() {
		return saleUserIds;
	}

	public void setSaleUserIds(List<Long> saleUserIds) {
		this.saleUserIds = saleUserIds;
	}

	public Boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	public Boolean getIsForRent() {
		return isForRent;
	}

	public void setIsForRent(Boolean isForRent) {
		this.isForRent = isForRent;
	}

	public BigDecimal getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(BigDecimal minPrice) {
		this.minPrice = minPrice;
	}

	public BigDecimal getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(BigDecimal maxPrice) {
		this.maxPrice = maxPrice;
	}

	public LocalDateTime getCreatedFrom() {
		return createdFrom;
	}

	public void setCreatedFrom(LocalDateTime createdFrom) {
		this.createdFrom = createdFrom;
	}

	public LocalDateTime getCreatedTo() {
		return createdTo;
	}

	public void setCreatedTo(LocalDateTime createdTo) {
		this.createdTo = createdTo;
	}

	public LocalDateTime getUpdatedFrom() {
		return updatedFrom;
	}

	public void setUpdatedFrom(LocalDateTime updatedFrom) {
		this.updatedFrom = updatedFrom;
	}

	public LocalDateTime getUpdatedTo() {
		return updatedTo;
	}

	public void setUpdatedTo(LocalDateTime updatedTo) {
		this.updatedTo = updatedTo;
	}

	public List<PropertyDetailFilterReq> getDetails() {
		return details;
	}

	public void setDetails(List<PropertyDetailFilterReq> details) {
		this.details = details;
	}
}

