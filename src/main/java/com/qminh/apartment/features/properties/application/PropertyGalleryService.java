package com.qminh.apartment.features.properties.application;

import com.qminh.apartment.features.files.domain.StoredFileMeta;
import com.qminh.apartment.features.files.persistence.StoredFileMetaRepository;
import com.qminh.apartment.features.properties.domain.Property;
import com.qminh.apartment.features.properties.domain.PropertyGallery;
import com.qminh.apartment.features.properties.domain.PropertyGalleryId;
import com.qminh.apartment.features.properties.dto.property_gallery.PropertyGalleryRes;
import com.qminh.apartment.shared.exception.ConflictException;
import com.qminh.apartment.shared.exception.ResourceNotFoundException;
import com.qminh.apartment.features.properties.mapper.PropertyGalleryMapper;
import com.qminh.apartment.features.properties.persistence.PropertyGalleryRepository;
import com.qminh.apartment.features.properties.persistence.PropertyRepository;
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
