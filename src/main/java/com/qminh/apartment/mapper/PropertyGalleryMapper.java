package com.qminh.apartment.mapper;

import com.qminh.apartment.dto.property_gallery.PropertyGalleryRes;
import com.qminh.apartment.entity.PropertyGallery;
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
	@Mapping(target = "filePath", source = "file.filePath")
	PropertyGalleryRes toRes(PropertyGallery entity);
}



