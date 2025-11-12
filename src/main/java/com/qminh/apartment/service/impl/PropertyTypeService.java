package com.qminh.apartment.service.impl;

import com.qminh.apartment.dto.property_type.PropertyTypeCreateReq;
import com.qminh.apartment.dto.property_type.PropertyTypeRes;
import com.qminh.apartment.dto.property_type.PropertyTypeUpdateReq;
import com.qminh.apartment.entity.PropertyType;
import com.qminh.apartment.exception.ResourceNotFoundException;
import com.qminh.apartment.mapper.PropertyTypeMapper;
import com.qminh.apartment.repository.PropertyTypeRepository;
import com.qminh.apartment.service.IPropertyTypeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class PropertyTypeService implements IPropertyTypeService {

	private final PropertyTypeRepository repository;
	private final PropertyTypeMapper mapper;
	private static final String NOT_FOUND = "Type not found: ";

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
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		return mapper.toRes(type);
	}

	@Transactional(readOnly = true)
	public Page<PropertyTypeRes> list(Pageable pageable) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		return repository.findAll(pageable).map(mapper::toRes);
	}

	@Transactional
	public PropertyTypeRes update(int id, PropertyTypeUpdateReq req) {
		PropertyType type = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		mapper.updateEntityFromReq(req, type);
		PropertyType updated = repository.save(Objects.requireNonNull(type, "type must not be null"));
		return mapper.toRes(updated);
	}

	@Transactional
	public void delete(int id) {
		PropertyType type = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		repository.delete(Objects.requireNonNull(type, "type must not be null"));
	}
}


