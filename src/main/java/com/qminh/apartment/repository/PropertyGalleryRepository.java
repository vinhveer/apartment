package com.qminh.apartment.repository;

import com.qminh.apartment.entity.PropertyGallery;
import com.qminh.apartment.entity.PropertyGalleryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyGalleryRepository extends JpaRepository<PropertyGallery, PropertyGalleryId> {

	List<PropertyGallery> findByProperty_PropertyId(Long propertyId);
}



