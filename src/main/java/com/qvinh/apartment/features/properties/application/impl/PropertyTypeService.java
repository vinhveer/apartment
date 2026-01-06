package com.qvinh.apartment.features.properties.application.impl;

import com.qvinh.apartment.features.properties.application.IPropertyTypeService;
import com.qvinh.apartment.features.properties.domain.PropertyType;
import com.qvinh.apartment.features.properties.dto.property_type.PropertyTypeCreateReq;
import com.qvinh.apartment.features.properties.dto.property_type.PropertyTypeRes;
import com.qvinh.apartment.features.properties.dto.property_type.PropertyTypeUpdateReq;
import com.qvinh.apartment.shared.error.ErrorCode;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.features.properties.mapper.PropertyTypeMapper;
import com.qvinh.apartment.features.properties.persistence.PropertyTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class PropertyTypeService implements IPropertyTypeService {

	private final PropertyTypeRepository repository;
	private final PropertyTypeMapper mapper;

	public PropertyTypeService(PropertyTypeRepository repository, PropertyTypeMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Transactional
	public PropertyTypeRes create(PropertyTypeCreateReq req) {
		PropertyType type = mapper.toEntity(req);
		PropertyType updated = repository.save(Objects.requireNonNull(type, "type must not be null"));
		PropertyType saved = Objects.requireNonNull(updated, "updated must not be null");
		return mapper.toRes(saved);
	}

	@Transactional(readOnly = true)
	public PropertyTypeRes get(int id) {
		PropertyType type = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_TYPE_NOT_FOUND, "Property type not found"));
		return mapper.toRes(type);
	}

	@Transactional(readOnly = true)
	public Page<PropertyTypeRes> list(Pageable pageable) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		return repository.findAll(pageable).map(mapper::toRes);
	}

	@Transactional(readOnly = true)
	public Page<PropertyTypeRes> search(String keyword, Pageable pageable) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		if (keyword == null || keyword.trim().isEmpty()) {
			return repository.findAll(pageable).map(mapper::toRes);
		}
		return repository.findByTypeNameContainingIgnoreCase(keyword.trim(), pageable)
			.map(mapper::toRes);
	}

	@Transactional
	public PropertyTypeRes update(int id, PropertyTypeUpdateReq req) {
		PropertyType type = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_TYPE_NOT_FOUND, "Property type not found"));
		mapper.updateEntityFromReq(req, type);
		PropertyType updated = repository.save(Objects.requireNonNull(type, "type must not be null"));
		return mapper.toRes(updated);
	}

	@Transactional
	public void delete(int id) {
		PropertyType type = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_TYPE_NOT_FOUND, "Property type not found"));
		repository.delete(Objects.requireNonNull(type, "type must not be null"));
	}
}
