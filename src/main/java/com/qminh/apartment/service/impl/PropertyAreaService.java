package com.qminh.apartment.service.impl;
import com.qminh.apartment.dto.area.PropertyAreaCreateReq;
import com.qminh.apartment.dto.area.PropertyAreaRes;
import com.qminh.apartment.dto.area.PropertyAreaUpdateReq;
import com.qminh.apartment.entity.PropertyArea;
import com.qminh.apartment.exception.ResourceNotFoundException;
import com.qminh.apartment.repository.PropertyAreaRepository;
import com.qminh.apartment.service.IPropertyAreaService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class PropertyAreaService implements IPropertyAreaService {

	private final PropertyAreaRepository repository;
	private static final String NOT_FOUND = "Area not found: ";

	public PropertyAreaService(PropertyAreaRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public PropertyAreaRes create(PropertyAreaCreateReq req) {
		PropertyArea area = new PropertyArea();
		area.setAreaName(req.getAreaName());
		area.setAreaLink(req.getAreaLink());
		PropertyArea saved = repository.save(area);
		return toRes(saved);
	}

	@Transactional(readOnly = true)
	public PropertyAreaRes get(int id) {
		PropertyArea area = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		return toRes(area);
	}

	@Transactional(readOnly = true)
	public Page<PropertyAreaRes> list(Pageable pageable) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		return repository.findAll(pageable).map(this::toRes);
	}

	@Transactional
	public PropertyAreaRes update(int id, PropertyAreaUpdateReq req) {
		PropertyArea area = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		area.setAreaName(req.getAreaName());
		area.setAreaLink(req.getAreaLink());
		return toRes(repository.save(area));
	}

	@Transactional
	public void delete(int id) {
		PropertyArea area = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		repository.delete(Objects.requireNonNull(area, "area must not be null"));
	}

	private PropertyAreaRes toRes(PropertyArea area) {
		PropertyArea nonNullArea = Objects.requireNonNull(area, "area must not be null");
		PropertyAreaRes res = new PropertyAreaRes();
		res.setAreaId(nonNullArea.getAreaId());
		res.setAreaName(nonNullArea.getAreaName());
		res.setAreaLink(nonNullArea.getAreaLink());
		return res;
	}
}


