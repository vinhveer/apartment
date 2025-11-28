package com.qminh.apartment.service.impl;

import com.qminh.apartment.dto.property_details.PropertyDetailsCreateReq;
import com.qminh.apartment.dto.property_details.PropertyDetailsRes;
import com.qminh.apartment.dto.property_details.PropertyDetailsUpdateReq;
import com.qminh.apartment.entity.Property;
import com.qminh.apartment.entity.PropertyDefineDetails;
import com.qminh.apartment.entity.PropertyDetails;
import com.qminh.apartment.entity.PropertyDetailsId;
import com.qminh.apartment.exception.BusinessException;
import com.qminh.apartment.exception.ConflictException;
import com.qminh.apartment.exception.ResourceNotFoundException;
import com.qminh.apartment.mapper.PropertyDetailsMapper;
import com.qminh.apartment.repository.PropertyDefineDetailsRepository;
import com.qminh.apartment.repository.PropertyDetailsRepository;
import com.qminh.apartment.repository.PropertyRepository;
import com.qminh.apartment.service.IPropertyDetailsService;
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
	private static final String PROPERTY_NOT_FOUND = "Property not found: ";
	private static final String DEFINE_NOT_FOUND_PREFIX = "Define details not found: ";

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
			.orElseThrow(() -> new ResourceNotFoundException(PROPERTY_NOT_FOUND + propertyId));

		List<Integer> reqIds = Objects.requireNonNull(req.getItems()).stream()
			.map(it -> Objects.requireNonNull(it.getDetailId()))
			.distinct()
			.collect(Collectors.toList());

		List<PropertyDefineDetails> defines = defineRepository.findAllById(Objects.requireNonNull(reqIds));
		if (defines.size() != reqIds.size()) {
			Set<Integer> found = defines.stream().map(PropertyDefineDetails::getDetailId).collect(Collectors.toSet());
			List<Integer> missing = reqIds.stream().filter(id -> !found.contains(id)).toList();
			throw new ResourceNotFoundException(DEFINE_NOT_FOUND_PREFIX + missing);
		}

		long existing = repository.countById_PropertyIdAndId_DetailIdIn(propertyId, reqIds);
		if (existing > 0) {
			throw new ConflictException("Some details already exist for property " + propertyId + " with detailIds " + reqIds);
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
			.orElseThrow(() -> new ResourceNotFoundException(PROPERTY_NOT_FOUND + propertyId)));

		List<Integer> reqIds = Objects.requireNonNull(req.getItems()).stream()
			.map(it -> Objects.requireNonNull(it.getDetailId()))
			.distinct()
			.collect(Collectors.toList());

		// Verify all target details exist
		Map<Integer, PropertyDetails> existingMap = new HashMap<>();
		for (Integer detailId : reqIds) {
			PropertyDetails pd = repository.findById_DetailIdAndId_PropertyId(detailId, propertyId)
				.orElseThrow(() -> new ResourceNotFoundException("Property detail not found for property " + propertyId + " and detailId " + detailId));
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
			.orElseThrow(() -> new ResourceNotFoundException(PROPERTY_NOT_FOUND + propertyId));
		return repository.findByProperty_PropertyId(property.getPropertyId())
			.stream().map(mapper::toRes).toList();
	}

	@Transactional
	public void deleteOne(Long propertyId, Integer detailId) {
		PropertyDetails entity = repository.findById_DetailIdAndId_PropertyId(
			Objects.requireNonNull(detailId), Objects.requireNonNull(propertyId)
		).orElseThrow(() -> new ResourceNotFoundException("Property detail not found for property " + propertyId + " and detailId " + detailId));
		repository.delete(Objects.requireNonNull(entity));
	}

	@Transactional
	public void deleteAll(Long propertyId) {
		Objects.requireNonNull(propertyRepository.findById(Objects.requireNonNull(propertyId))
			.orElseThrow(() -> new ResourceNotFoundException(PROPERTY_NOT_FOUND + propertyId)));
		repository.deleteById_PropertyId(propertyId);
	}
}


