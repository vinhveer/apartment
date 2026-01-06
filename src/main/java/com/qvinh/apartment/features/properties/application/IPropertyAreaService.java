package com.qvinh.apartment.features.properties.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.qvinh.apartment.features.properties.dto.property_area.PropertyAreaCreateReq;
import com.qvinh.apartment.features.properties.dto.property_area.PropertyAreaRes;
import com.qvinh.apartment.features.properties.dto.property_area.PropertyAreaUpdateReq;

public interface IPropertyAreaService {
	PropertyAreaRes create(PropertyAreaCreateReq req);
	PropertyAreaRes get(int id);
	Page<PropertyAreaRes> list(Pageable pageable);
	PropertyAreaRes update(int id, PropertyAreaUpdateReq req);
	void delete(int id);
}
