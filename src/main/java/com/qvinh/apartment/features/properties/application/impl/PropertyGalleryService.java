package com.qvinh.apartment.features.properties.application.impl;

import com.qvinh.apartment.features.properties.application.IPropertyGalleryService;
import com.qvinh.apartment.features.files.domain.StoredFileMeta;
import com.qvinh.apartment.features.files.persistence.StoredFileMetaRepository;
import com.qvinh.apartment.features.properties.domain.Property;
import com.qvinh.apartment.features.properties.domain.PropertyGallery;
import com.qvinh.apartment.features.properties.domain.PropertyGalleryId;
import com.qvinh.apartment.features.properties.dto.property_gallery.PropertyGalleryRes;
import com.qvinh.apartment.shared.error.ErrorCode;
import com.qvinh.apartment.shared.exception.ConflictException;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.features.properties.mapper.PropertyGalleryMapper;
import com.qvinh.apartment.features.properties.persistence.PropertyGalleryRepository;
import com.qvinh.apartment.features.properties.persistence.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class PropertyGalleryService implements IPropertyGalleryService {

	private final PropertyRepository propertyRepository;
	private final StoredFileMetaRepository storedFileMetaRepository;
	private final PropertyGalleryRepository galleryRepository;
	private final PropertyGalleryMapper mapper;

	public PropertyGalleryService(PropertyRepository propertyRepository,
	                              StoredFileMetaRepository storedFileMetaRepository,
	                              PropertyGalleryRepository galleryRepository,
	                              PropertyGalleryMapper mapper) {
		this.propertyRepository = propertyRepository;
		this.storedFileMetaRepository = storedFileMetaRepository;
		this.galleryRepository = galleryRepository;
		this.mapper = mapper;
	}

	@Transactional
	public PropertyGalleryRes addFileIntoGallery(Long propertyId, Long fileId) {
		Property property = propertyRepository.findById(Objects.requireNonNull(propertyId))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found"));
		StoredFileMeta file = storedFileMetaRepository.findById(Objects.requireNonNull(fileId))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FILE_NOT_FOUND, "File not found"));

		PropertyGalleryId id = new PropertyGalleryId(propertyId, fileId);
		if (galleryRepository.existsById(id)) {
			throw new ConflictException(ErrorCode.FILE_ALREADY_IN_GALLERY, "File already exists in gallery");
		}

		PropertyGallery entity = new PropertyGallery();
		entity.setId(id);
		entity.setProperty(property);
		entity.setFile(file);
		LocalDateTime now = LocalDateTime.now();
		entity.setCreatedAt(now);
		entity.setUpdatedAt(now);

		PropertyGallery saved = galleryRepository.save(entity);
		return mapper.toRes(saved);
	}

	@Transactional(readOnly = true)
	public List<PropertyGalleryRes> getFileByPropertiesId(Long propertyId) {
		Objects.requireNonNull(propertyRepository.findById(Objects.requireNonNull(propertyId))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found")));
		return galleryRepository.findByProperty_PropertyId(propertyId)
			.stream()
			.map(mapper::toRes)
			.toList();
	}

	@Transactional
	public void deleteFileIntoGallery(Long propertyId, Long fileId) {
		PropertyGalleryId id = new PropertyGalleryId(
			Objects.requireNonNull(propertyId),
			Objects.requireNonNull(fileId)
		);
		PropertyGallery entity = galleryRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_GALLERY_NOT_FOUND, "Property gallery not found"));
		galleryRepository.delete(Objects.requireNonNull(entity));
	}
}
