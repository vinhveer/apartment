package com.qvinh.apartment.features.properties.application.impl;

import com.qvinh.apartment.features.properties.application.IPropertyDetailsService;
import com.qvinh.apartment.features.properties.constants.PropertiesMessages;
import com.qvinh.apartment.features.properties.domain.Property;
import com.qvinh.apartment.features.properties.domain.PropertyDefineDetails;
import com.qvinh.apartment.features.properties.domain.PropertyDetails;
import com.qvinh.apartment.features.properties.domain.PropertyDetailsId;
import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsCreateReq;
import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsRes;
import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsUpdateReq;
import com.qvinh.apartment.shared.error.ErrorCode;
import com.qvinh.apartment.shared.exception.BusinessException;
import com.qvinh.apartment.shared.exception.ConflictException;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.features.properties.mapper.PropertyDetailsMapper;
import com.qvinh.apartment.features.properties.persistence.PropertyDefineDetailsRepository;
import com.qvinh.apartment.features.properties.persistence.PropertyDetailsRepository;
import com.qvinh.apartment.features.properties.persistence.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
public class PropertyDetailsService implements IPropertyDetailsService {

	private final PropertyRepository propertyRepository;
	private final PropertyDefineDetailsRepository defineRepository;
	private final PropertyDetailsRepository repository;
	private final PropertyDetailsMapper mapper;

	public PropertyDetailsService(PropertyRepository propertyRepository,
	                              PropertyDefineDetailsRepository defineRepository,
	                              PropertyDetailsRepository repository,
	                              PropertyDetailsMapper mapper) {
		this.propertyRepository = propertyRepository;
		this.defineRepository = defineRepository;
		this.repository = repository;
		this.mapper = mapper;
	}

	@Transactional
	public List<PropertyDetailsRes> create(Long propertyId, PropertyDetailsCreateReq req) {
		Property property = propertyRepository.findById(Objects.requireNonNull(propertyId))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_NOT_FOUND, PropertiesMessages.PROPERTY_NOT_FOUND));

		List<Integer> reqIds = Objects.requireNonNull(req.getItems()).stream()
			.map(it -> Objects.requireNonNull(it.getDetailId()))
			.distinct()
			.toList();

		List<PropertyDefineDetails> defines = defineRepository.findAllById(Objects.requireNonNull(reqIds));
		if (defines.size() != reqIds.size()) {
			throw new ResourceNotFoundException(ErrorCode.PROPERTY_DEFINE_DETAIL_NOT_FOUND, PropertiesMessages.DEFINE_DETAILS_NOT_FOUND);
		}

		long existing = repository.countById_PropertyIdAndId_DetailIdIn(propertyId, reqIds);
		if (existing > 0) {
			throw new ConflictException(ErrorCode.PROPERTY_DETAILS_CONFLICT, PropertiesMessages.PROPERTY_DETAILS_CONFLICT);
		}

		Map<Integer, PropertyDefineDetails> defById = defines.stream()
			.collect(Collectors.toMap(PropertyDefineDetails::getDetailId, d -> d));

		List<PropertyDetails> entities = new ArrayList<>();
		req.getItems().forEach(it -> {
			PropertyDefineDetails def = defById.get(it.getDetailId());
			if (Boolean.TRUE.equals(Objects.requireNonNull(def).getIsNumber())) {
				String v = it.getValue();
				if (v != null && !v.isBlank()) {
					try {
						new BigDecimal(v.trim());
					} catch (NumberFormatException nfe) {
						throw new BusinessException("Value must be numeric for detailId " + it.getDetailId());
					}
				}
			}
			PropertyDetails e = new PropertyDetails();
			e.setId(new PropertyDetailsId(it.getDetailId(), propertyId));
			e.setProperty(property);
			e.setDetail(def);
			e.setValue(it.getValue());
			entities.add(e);
		});

		List<PropertyDetails> saved = repository.saveAll(entities);
		return saved.stream().map(mapper::toRes).toList();
	}

	@Transactional
	public List<PropertyDetailsRes> update(Long propertyId, PropertyDetailsUpdateReq req) {
		Objects.requireNonNull(propertyRepository.findById(Objects.requireNonNull(propertyId))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_NOT_FOUND, PropertiesMessages.PROPERTY_NOT_FOUND)));

		List<Integer> reqIds = Objects.requireNonNull(req.getItems()).stream()
			.map(it -> Objects.requireNonNull(it.getDetailId()))
			.distinct()
			.toList();

		// Verify all target details exist
		Map<Integer, PropertyDetails> existingMap = new HashMap<>();
		for (Integer detailId : reqIds) {
			PropertyDetails pd = repository.findById_DetailIdAndId_PropertyId(detailId, propertyId)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_DETAIL_NOT_FOUND, PropertiesMessages.PROPERTY_DETAIL_NOT_FOUND));
			existingMap.put(detailId, pd);
		}

		// Apply updates
		req.getItems().forEach(it -> {
			PropertyDetails pd = existingMap.get(it.getDetailId());
			if (Boolean.TRUE.equals(Objects.requireNonNull(pd.getDetail()).getIsNumber())) {
				String v = it.getValue();
				if (v != null && !v.isBlank()) {
					try {
						new BigDecimal(v.trim());
					} catch (NumberFormatException nfe) {
						throw new BusinessException("Value must be numeric for detailId " + it.getDetailId());
					}
				}
			}
			pd.setValue(it.getValue());
		});

		List<PropertyDetails> toUpdate = List.copyOf(existingMap.values());
		List<PropertyDetails> updated = repository.saveAll(Objects.requireNonNull(toUpdate));
		return updated.stream().map(mapper::toRes).toList();
	}

	@Transactional(readOnly = true)
	public List<PropertyDetailsRes> listByProperty(Long propertyId) {
		Property property = propertyRepository.findById(Objects.requireNonNull(propertyId))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_NOT_FOUND, PropertiesMessages.PROPERTY_NOT_FOUND));
		return repository.findByProperty_PropertyId(property.getPropertyId())
			.stream().map(mapper::toRes).toList();
	}

	@Transactional
	public void deleteOne(Long propertyId, Integer detailId) {
		PropertyDetails entity = repository.findById_DetailIdAndId_PropertyId(
			Objects.requireNonNull(detailId), Objects.requireNonNull(propertyId)
		).orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_DETAIL_NOT_FOUND, PropertiesMessages.PROPERTY_DETAIL_NOT_FOUND));
		repository.delete(Objects.requireNonNull(entity));
	}

	@Transactional
	public void deleteAll(Long propertyId) {
		Objects.requireNonNull(propertyRepository.findById(Objects.requireNonNull(propertyId))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PROPERTY_NOT_FOUND, PropertiesMessages.PROPERTY_NOT_FOUND)));
		repository.deleteById_PropertyId(propertyId);
	}
}
