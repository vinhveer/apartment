package com.qvinh.apartment.features.properties.application.impl;

import com.qvinh.apartment.features.properties.application.IPropertyService;
import com.qvinh.apartment.features.accounts.domain.PropertySaleInfo;
import com.qvinh.apartment.features.accounts.persistence.PropertySaleInfoRepository;
import com.qvinh.apartment.features.properties.constants.PropertiesMessages;
import com.qvinh.apartment.features.properties.domain.Property;
import com.qvinh.apartment.features.properties.domain.PropertyArea;
import com.qvinh.apartment.features.properties.domain.PropertyType;
import com.qvinh.apartment.features.properties.dto.property.PropertyCreateReq;
import com.qvinh.apartment.features.properties.dto.property.PropertyRes;
import com.qvinh.apartment.features.properties.dto.property.PropertySearchReq;
import com.qvinh.apartment.features.properties.dto.property.PropertySelectRes;
import com.qvinh.apartment.features.properties.dto.property.PropertyUpdateReq;
import com.qvinh.apartment.shared.error.ErrorCode;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.features.properties.mapper.PropertyMapper;
import com.qvinh.apartment.features.properties.persistence.PropertyAreaRepository;
import com.qvinh.apartment.features.properties.persistence.PropertyRepository;
import com.qvinh.apartment.features.properties.persistence.PropertySpecifications;
import com.qvinh.apartment.features.properties.persistence.PropertyTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class PropertyService implements IPropertyService {

	private static final String PAGEABLE_NOT_NULL = "pageable must not be null";

	private final PropertyRepository repository;
	private final PropertyMapper mapper;
	private final PropertyTypeRepository typeRepository;
	private final PropertySaleInfoRepository saleInfoRepository;
	private final PropertyAreaRepository areaRepository;

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
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_TYPE_NOT_FOUND, PropertiesMessages.PROPERTY_TYPE_NOT_FOUND));
		PropertySaleInfo sale = saleInfoRepository.findByUserId(Objects.requireNonNull(req.getSaleUserId()))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_SALE_INFO_NOT_FOUND, PropertiesMessages.PROPERTY_SALE_INFO_NOT_FOUND));
		PropertyArea area = areaRepository.findById(Objects.requireNonNull(req.getAreaId()))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_AREA_NOT_FOUND, PropertiesMessages.PROPERTY_AREA_NOT_FOUND));
		entity.setType(type);
		entity.setSaleInfo(sale);
		entity.setArea(area);
		Property saved = repository.save(entity);
		return mapper.toRes(Objects.requireNonNull(saved, "saved must not be null"));
	}

	@Transactional(readOnly = true)
	public PropertyRes get(long id) {
		Property entity = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_NOT_FOUND, PropertiesMessages.PROPERTY_NOT_FOUND));
		return mapper.toRes(entity);
	}

	@Transactional(readOnly = true)
	public PropertySelectRes getFull(long id) {
		Property entity = repository.findByIdWithRelations(id);
		if (entity == null) {
			throw new ResourceNotFoundException(ErrorCode.PROPERTY_NOT_FOUND, PropertiesMessages.PROPERTY_NOT_FOUND);
		}
		return mapper.toSelectRes(entity);
	}

	@Transactional(readOnly = true)
	public Page<PropertyRes> list(Pageable pageable) {
		Objects.requireNonNull(pageable, PAGEABLE_NOT_NULL);
		return repository.findAll(pageable).map(mapper::toRes);
	}

	@Transactional(readOnly = true)
	public Page<PropertyRes> search(PropertySearchReq req, Pageable pageable) {
		Objects.requireNonNull(pageable, PAGEABLE_NOT_NULL);
		var spec = PropertySpecifications.bySearchReq(req);
		return repository.findAll(spec, pageable).map(mapper::toRes);
	}

	@Transactional(readOnly = true)
	public Page<PropertySelectRes> searchFull(PropertySearchReq req, Pageable pageable) {
		Objects.requireNonNull(pageable, PAGEABLE_NOT_NULL);
		var spec = PropertySpecifications.bySearchReq(req);
		return repository.findAllWithRelations(spec, pageable).map(mapper::toSelectRes);
	}

	@Transactional
	public PropertyRes update(long id, PropertyUpdateReq req) {
		Property entity = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_NOT_FOUND, PropertiesMessages.PROPERTY_NOT_FOUND));
		mapper.updateEntityFromReq(req, entity);
		PropertyType type = typeRepository.findById(Objects.requireNonNull(req.getTypeId()))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_TYPE_NOT_FOUND, PropertiesMessages.PROPERTY_TYPE_NOT_FOUND));
		PropertySaleInfo sale = saleInfoRepository.findByUserId(Objects.requireNonNull(req.getSaleUserId()))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_SALE_INFO_NOT_FOUND, PropertiesMessages.PROPERTY_SALE_INFO_NOT_FOUND));
		PropertyArea area = areaRepository.findById(Objects.requireNonNull(req.getAreaId()))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_AREA_NOT_FOUND, PropertiesMessages.PROPERTY_AREA_NOT_FOUND));
		entity.setType(type);
		entity.setSaleInfo(sale);
		entity.setArea(area);
		Property updated = repository.save(entity);
		return mapper.toRes(updated);
	}

	@Transactional
	public void delete(long id) {
		Property entity = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_NOT_FOUND, PropertiesMessages.PROPERTY_NOT_FOUND));
		repository.delete(Objects.requireNonNull(entity, "property must not be null"));
	}
}
