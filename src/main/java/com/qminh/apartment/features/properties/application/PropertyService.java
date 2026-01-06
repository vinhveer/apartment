package com.qminh.apartment.features.properties.application;

import com.qminh.apartment.features.accounts.domain.PropertySaleInfo;
import com.qminh.apartment.features.accounts.persistence.PropertySaleInfoRepository;
import com.qminh.apartment.features.properties.domain.Property;
import com.qminh.apartment.features.properties.domain.PropertyArea;
import com.qminh.apartment.features.properties.domain.PropertyType;
import com.qminh.apartment.features.properties.dto.property.PropertyCreateReq;
import com.qminh.apartment.features.properties.dto.property.PropertyRes;
import com.qminh.apartment.features.properties.dto.property.PropertySearchReq;
import com.qminh.apartment.features.properties.dto.property.PropertySelectRes;
import com.qminh.apartment.features.properties.dto.property.PropertyUpdateReq;
import com.qminh.apartment.shared.exception.ResourceNotFoundException;
import com.qminh.apartment.features.properties.mapper.PropertyMapper;
import com.qminh.apartment.features.properties.persistence.PropertyAreaRepository;
import com.qminh.apartment.features.properties.persistence.PropertyRepository;
import com.qminh.apartment.features.properties.persistence.PropertySpecifications;
import com.qminh.apartment.features.properties.persistence.PropertyTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class PropertyService implements IPropertyService {

	private final PropertyRepository repository;
	private final PropertyMapper mapper;
	private final PropertyTypeRepository typeRepository;
	private final PropertySaleInfoRepository saleInfoRepository;
	private final PropertyAreaRepository areaRepository;
	private static final String NOT_FOUND = "Property not found: ";

	public PropertyService(PropertyRepository repository, PropertyMapper mapper,
	                       PropertyTypeRepository typeRepository,
	                       PropertySaleInfoRepository saleInfoRepository,
	                       PropertyAreaRepository areaRepository) {
		this.repository = repository;
		this.mapper = mapper;
		this.typeRepository = typeRepository;
		this.saleInfoRepository = saleInfoRepository;
		this.areaRepository = areaRepository;
	}

	@Transactional
	public PropertyRes create(PropertyCreateReq req) {
		Property entity = mapper.toEntity(req);
		PropertyType type = typeRepository.findById(Objects.requireNonNull(req.getTypeId()))
			.orElseThrow(() -> new ResourceNotFoundException("Type not found: " + req.getTypeId()));
		PropertySaleInfo sale = saleInfoRepository.findByUserId(Objects.requireNonNull(req.getSaleUserId()))
			.orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + req.getSaleUserId()));
		PropertyArea area = areaRepository.findById(Objects.requireNonNull(req.getAreaId()))
			.orElseThrow(() -> new ResourceNotFoundException("Area not found: " + req.getAreaId()));
		entity.setType(type);
		entity.setSaleInfo(sale);
		entity.setArea(area);
		Property saved = repository.save(entity);
		return mapper.toRes(Objects.requireNonNull(saved, "saved must not be null"));
	}

	@Transactional(readOnly = true)
	public PropertyRes get(long id) {
		Property entity = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		return mapper.toRes(entity);
	}

	@Transactional(readOnly = true)
	public PropertySelectRes getFull(long id) {
		Property entity = repository.findByIdWithRelations(id);
		if (entity == null) {
			throw new ResourceNotFoundException(NOT_FOUND + id);
		}
		return mapper.toSelectRes(entity);
	}

	@Transactional(readOnly = true)
	public Page<PropertyRes> list(Pageable pageable) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		return repository.findAll(pageable).map(mapper::toRes);
	}

	@Transactional(readOnly = true)
	public Page<PropertyRes> search(PropertySearchReq req, Pageable pageable) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		var spec = PropertySpecifications.bySearchReq(req);
		return repository.findAll(spec, pageable).map(mapper::toRes);
	}

	@Transactional(readOnly = true)
	public Page<PropertySelectRes> searchFull(PropertySearchReq req, Pageable pageable) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		var spec = PropertySpecifications.bySearchReq(req);
		return repository.findAllWithRelations(spec, pageable).map(mapper::toSelectRes);
	}

	@Transactional
	public PropertyRes update(long id, PropertyUpdateReq req) {
		Property entity = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		mapper.updateEntityFromReq(req, entity);
		PropertyType type = typeRepository.findById(Objects.requireNonNull(req.getTypeId()))
			.orElseThrow(() -> new ResourceNotFoundException("Type not found: " + req.getTypeId()));
		PropertySaleInfo sale = saleInfoRepository.findByUserId(Objects.requireNonNull(req.getSaleUserId()))
			.orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + req.getSaleUserId()));
		PropertyArea area = areaRepository.findById(Objects.requireNonNull(req.getAreaId()))
			.orElseThrow(() -> new ResourceNotFoundException("Area not found: " + req.getAreaId()));
		entity.setType(type);
		entity.setSaleInfo(sale);
		entity.setArea(area);
		Property updated = repository.save(entity);
		return mapper.toRes(updated);
	}

	@Transactional
	public void delete(long id) {
		Property entity = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		repository.delete(Objects.requireNonNull(entity, "property must not be null"));
	}
}
