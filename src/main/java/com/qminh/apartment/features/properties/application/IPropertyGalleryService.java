package com.qminh.apartment.features.properties.application;

import com.qminh.apartment.features.properties.dto.property_gallery.PropertyGalleryRes;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IPropertyGalleryService {

	@Transactional
	PropertyGalleryRes addFileIntoGallery(Long propertyId, Long fileId);

	@Transactional(readOnly = true)
	List<PropertyGalleryRes> getFileByPropertiesId(Long propertyId);

	@Transactional
	void deleteFileIntoGallery(Long propertyId, Long fileId);
}

