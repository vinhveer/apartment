package com.qvinh.apartment.features.properties.application.impl;

import com.qvinh.apartment.features.properties.application.IPropertyAreaService;
import com.qvinh.apartment.features.properties.domain.PropertyArea;
import com.qvinh.apartment.features.properties.dto.property_area.PropertyAreaCreateReq;
import com.qvinh.apartment.features.properties.dto.property_area.PropertyAreaRes;
import com.qvinh.apartment.features.properties.dto.property_area.PropertyAreaUpdateReq;
import com.qvinh.apartment.shared.error.ErrorCode;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.features.properties.mapper.PropertyAreaMapper;
import com.qvinh.apartment.features.properties.persistence.PropertyAreaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class PropertyAreaService implements IPropertyAreaService {

	private final PropertyAreaRepository repository;
	private final PropertyAreaMapper mapper;

	public PropertyAreaService(PropertyAreaRepository repository, PropertyAreaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Transactional
	public PropertyAreaRes create(PropertyAreaCreateReq req) {
		PropertyArea area = mapper.toEntity(req);
		PropertyArea updated = repository.save(Objects.requireNonNull(area, "area must not be null"));
		PropertyArea saved = Objects.requireNonNull(updated, "updated must not be null");
		return mapper.toRes(saved);
	}

	@Transactional(readOnly = true)
	public PropertyAreaRes get(int id) {
		PropertyArea area = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_AREA_NOT_FOUND, "Area not found"));
		return mapper.toRes(area);
	}

	@Transactional(readOnly = true)
	public Page<PropertyAreaRes> list(Pageable pageable) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		return repository.findAll(pageable).map(mapper::toRes);
	}

	@Transactional
	public PropertyAreaRes update(int id, PropertyAreaUpdateReq req) {
		PropertyArea area = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_AREA_NOT_FOUND, "Area not found"));
		mapper.updateEntityFromReq(req, area);
		PropertyArea updated = repository.save(Objects.requireNonNull(area, "area must not be null"));
		return mapper.toRes(updated);
	}

	@Transactional
	public void delete(int id) {
		PropertyArea area = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_AREA_NOT_FOUND, "Area not found"));
		repository.delete(Objects.requireNonNull(area, "area must not be null"));
	}
}
