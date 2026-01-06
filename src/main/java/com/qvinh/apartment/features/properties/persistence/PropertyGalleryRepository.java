package com.qvinh.apartment.features.properties.persistence;

import com.qvinh.apartment.features.properties.domain.PropertyGallery;
import com.qvinh.apartment.features.properties.domain.PropertyGalleryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyGalleryRepository extends JpaRepository<PropertyGallery, PropertyGalleryId> {

	List<PropertyGallery> findByProperty_PropertyId(Long propertyId);
}


