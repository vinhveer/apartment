package com.qminh.apartment.features.properties.mapper;

import com.qminh.apartment.features.properties.dto.property_gallery.PropertyGalleryRes;
import com.qminh.apartment.features.properties.domain.PropertyGallery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
	componentModel = "spring",
	nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PropertyGalleryMapper {

	@Mapping(target = "propertyId", source = "property.propertyId")
	@Mapping(target = "fileId", source = "file.fileId")
	@Mapping(target = "filePath", source = "file.relativePath")
	PropertyGalleryRes toRes(PropertyGallery entity);
}


