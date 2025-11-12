package com.qminh.apartment.service.impl;

import com.qminh.apartment.dto.property_define_details.PropertyDefineDetailsCreateReq;
import com.qminh.apartment.dto.property_define_details.PropertyDefineDetailsRes;
import com.qminh.apartment.dto.property_define_details.PropertyDefineDetailsUpdateReq;
import com.qminh.apartment.entity.PropertyDefineDetails;
import com.qminh.apartment.exception.ResourceNotFoundException;
import com.qminh.apartment.mapper.PropertyDefineDetailsMapper;
import com.qminh.apartment.repository.PropertyDefineDetailsRepository;
import com.qminh.apartment.service.IPropertyDefineDetailsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class PropertyDefineDetailsService implements IPropertyDefineDetailsService {

	private final PropertyDefineDetailsRepository repository;
	private final PropertyDefineDetailsMapper mapper;
	private static final String NOT_FOUND = "Property define detail not found: ";

	public PropertyDefineDetailsService(PropertyDefineDetailsRepository repository, PropertyDefineDetailsMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Transactional
	public PropertyDefineDetailsRes create(PropertyDefineDetailsCreateReq req) {
		PropertyDefineDetails entity = mapper.toEntity(req);
		PropertyDefineDetails updated = repository.save(Objects.requireNonNull(entity, "entity must not be null"));
		PropertyDefineDetails saved = Objects.requireNonNull(updated, "updated must not be null");
		return mapper.toRes(saved);
	}

	@Transactional(readOnly = true)
	public PropertyDefineDetailsRes get(int id) {
		PropertyDefineDetails entity = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
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
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		mapper.updateEntityFromReq(req, entity);
		PropertyDefineDetails updated = repository.save(Objects.requireNonNull(entity, "entity must not be null"));
		return mapper.toRes(updated);
	}

	@Transactional
	public void delete(int id) {
		PropertyDefineDetails entity = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		repository.delete(Objects.requireNonNull(entity, "entity must not be null"));
	}
}


