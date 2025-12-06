package com.qminh.apartment.service.impl;

import com.qminh.apartment.dto.property_gallery.PropertyGalleryRes;
import com.qminh.apartment.entity.Property;
import com.qminh.apartment.entity.PropertyGallery;
import com.qminh.apartment.entity.PropertyGalleryId;
import com.qminh.apartment.entity.StoredFileMeta;
import com.qminh.apartment.exception.ConflictException;
import com.qminh.apartment.exception.ResourceNotFoundException;
import com.qminh.apartment.mapper.PropertyGalleryMapper;
import com.qminh.apartment.repository.PropertyGalleryRepository;
import com.qminh.apartment.repository.PropertyRepository;
import com.qminh.apartment.repository.StoredFileMetaRepository;
import com.qminh.apartment.service.IPropertyGalleryService;
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
	private static final String PROPERTY_NOT_FOUND = "Property not found: ";
	private static final String FILE_NOT_FOUND = "File not found: ";

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
			.orElseThrow(() -> new ResourceNotFoundException(PROPERTY_NOT_FOUND + propertyId));
		StoredFileMeta file = storedFileMetaRepository.findById(Objects.requireNonNull(fileId))
			.orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND + fileId));

		PropertyGalleryId id = new PropertyGalleryId(propertyId, fileId);
		if (galleryRepository.existsById(id)) {
			throw new ConflictException("File already exists in gallery for property " + propertyId + " and file " + fileId);
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
			.orElseThrow(() -> new ResourceNotFoundException(PROPERTY_NOT_FOUND + propertyId)));
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
			.orElseThrow(() -> new ResourceNotFoundException(
				"Property gallery not found for property " + propertyId + " and file " + fileId
			));
		galleryRepository.delete(Objects.requireNonNull(entity));
	}
}



