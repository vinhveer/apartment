package com.qvinh.apartment.features.properties.application.impl;

import com.qvinh.apartment.features.properties.application.IPropertyDefineDetailsService;
import com.qvinh.apartment.features.properties.domain.PropertyDefineDetails;
import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsCreateReq;
import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsRes;
import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsUpdateReq;
import com.qvinh.apartment.shared.error.ErrorCode;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.features.properties.mapper.PropertyDefineDetailsMapper;
import com.qvinh.apartment.features.properties.persistence.PropertyDefineDetailsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class PropertyDefineDetailsService implements IPropertyDefineDetailsService {

	private final PropertyDefineDetailsRepository repository;
	private final PropertyDefineDetailsMapper mapper;
	private static final String ENTITY_NOT_NULL = "entity must not be null";

	public PropertyDefineDetailsService(PropertyDefineDetailsRepository repository, PropertyDefineDetailsMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Transactional
	public PropertyDefineDetailsRes create(PropertyDefineDetailsCreateReq req) {
		PropertyDefineDetails entity = mapper.toEntity(req);
		PropertyDefineDetails updated = repository.save(Objects.requireNonNull(entity, ENTITY_NOT_NULL));
		PropertyDefineDetails saved = Objects.requireNonNull(updated, "updated must not be null");
		return mapper.toRes(saved);
	}

	@Transactional(readOnly = true)
	public PropertyDefineDetailsRes get(int id) {
		PropertyDefineDetails entity = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_DEFINE_DETAIL_NOT_FOUND, "Property define detail not found"));
		return mapper.toRes(entity);
	}

	@Transactional(readOnly = true)
	public Page<PropertyDefineDetailsRes> list(Pageable pageable) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		return repository.findAll(pageable).map(mapper::toRes);
	}

	@Transactional
	public PropertyDefineDetailsRes update(int id, PropertyDefineDetailsUpdateReq req) {
		PropertyDefineDetails entity = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_DEFINE_DETAIL_NOT_FOUND, "Property define detail not found"));
		mapper.updateEntityFromReq(req, entity);
		PropertyDefineDetails updated = repository.save(Objects.requireNonNull(entity, ENTITY_NOT_NULL));
		return mapper.toRes(updated);
	}

	@Transactional
	public void delete(int id) {
		PropertyDefineDetails entity = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_DEFINE_DETAIL_NOT_FOUND, "Property define detail not found"));
		repository.delete(Objects.requireNonNull(entity, ENTITY_NOT_NULL));
	}
}
