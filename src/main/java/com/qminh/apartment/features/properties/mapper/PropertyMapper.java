package com.qminh.apartment.features.properties.mapper;

import com.qminh.apartment.features.files.domain.StoredFileMeta;
import com.qminh.apartment.features.properties.dto.property.*;
import com.qminh.apartment.features.properties.domain.Property;
import com.qminh.apartment.features.properties.domain.PropertyDefineDetails;
import com.qminh.apartment.features.properties.domain.PropertyDetails;
import com.qminh.apartment.features.properties.domain.PropertyGallery;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
	componentModel = "spring",
	nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PropertyMapper {

	@Mapping(target = "propertyId", ignore = true)
	@Mapping(target = "type", ignore = true)
	@Mapping(target = "saleInfo", ignore = true)
	@Mapping(target = "area", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "galleries", ignore = true)
	@Mapping(target = "details", ignore = true)
	Property toEntity(PropertyCreateReq req);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "propertyId", ignore = true)
	@Mapping(target = "type", ignore = true)
	@Mapping(target = "saleInfo", ignore = true)
	@Mapping(target = "area", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "galleries", ignore = true)
	@Mapping(target = "details", ignore = true)
	void updateEntityFromReq(PropertyUpdateReq req, @MappingTarget Property entity);

	@Mapping(target = "typeId", source = "type.typeId")
	@Mapping(target = "typeName", source = "type.typeName")
	@Mapping(target = "saleUserId", source = "saleInfo.userId")
	@Mapping(target = "saleDisplayName", source = "saleInfo.user.displayName")
	@Mapping(target = "salePhone", source = "saleInfo.phone")
	@Mapping(target = "areaId", source = "area.areaId")
	@Mapping(target = "areaName", source = "area.areaName")
	@Mapping(target = "mainImageRelativePath", expression = "java(extractMainImagePath(entity))")
	PropertyRes toRes(Property entity);

	default String extractMainImagePath(Property entity) {
		if (entity.getGalleries() == null || entity.getGalleries().isEmpty()) {
			return null;
		}
		return entity.getGalleries().stream()
			.findFirst()
			.map(g -> g.getFile() != null ? g.getFile().getRelativePath() : null)
			.orElse(null);
	}

	@Mapping(target = "type", expression = "java(toTypeInfo(entity))")
	@Mapping(target = "area", expression = "java(toAreaInfo(entity))")
	@Mapping(target = "saleInfo", expression = "java(toSaleInfoRes(entity))")
	@Mapping(target = "galleries", expression = "java(toGalleryResList(entity.getGalleries()))")
	@Mapping(target = "details", expression = "java(toDetailResList(entity.getDetails()))")
	PropertySelectRes toSelectRes(Property entity);

	default PropertyTypeInfo toTypeInfo(Property entity) {
		if (entity.getType() == null) return null;
		return new PropertyTypeInfo(entity.getType().getTypeId(), entity.getType().getTypeName());
	}

	default PropertyAreaInfo toAreaInfo(Property entity) {
		if (entity.getArea() == null) return null;
		return new PropertyAreaInfo(entity.getArea().getAreaId(), entity.getArea().getAreaName(), entity.getArea().getAreaLink());
	}

	default PropertySaleInfoRes toSaleInfoRes(Property entity) {
		if (entity.getSaleInfo() == null) return null;
		String displayName = entity.getSaleInfo().getUser() != null
			? entity.getSaleInfo().getUser().getDisplayName()
			: null;
		return new PropertySaleInfoRes(entity.getSaleInfo().getUserId(), displayName, entity.getSaleInfo().getPhone());
	}

	default List<PropertyGalleryRes> toGalleryResList(Set<PropertyGallery> galleries) {
		if (galleries == null) return List.of();
		return galleries.stream().map(this::toGalleryRes).collect(Collectors.toList());
	}

	default PropertyGalleryRes toGalleryRes(PropertyGallery gallery) {
		if (gallery == null || gallery.getFile() == null) return null;
		StoredFileMeta file = gallery.getFile();
		PropertyGalleryRes res = new PropertyGalleryRes();
		res.setFileId(file.getFileId());
		res.setOriginalName(file.getOriginalName());
		res.setRelativePath(file.getRelativePath());
		res.setMimeType(file.getMimeType());
		res.setAltText(file.getAltText());
		res.setTitle(file.getTitle());
		return res;
	}

	default List<PropertyDetailRes> toDetailResList(Set<PropertyDetails> details) {
		if (details == null) return List.of();
		return details.stream().map(this::toDetailRes).collect(Collectors.toList());
	}

	default PropertyDetailRes toDetailRes(PropertyDetails detail) {
		if (detail == null) return null;
		PropertyDefineDetails def = detail.getDetail();
		PropertyDetailRes res = new PropertyDetailRes();
		res.setDetailId(def != null ? def.getDetailId() : null);
		res.setDetailName(def != null ? def.getDetailName() : null);
		res.setIsNumber(def != null ? def.getIsNumber() : null);
		res.setUnit(def != null ? def.getUnit() : null);
		res.setShowInHomePage(def != null ? def.getShowInHomePage() : null);
		res.setValue(detail.getValue());
		return res;
	}
}

