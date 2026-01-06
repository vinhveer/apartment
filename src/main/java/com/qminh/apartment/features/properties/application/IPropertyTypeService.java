package com.qminh.apartment.features.properties.application;

import com.qminh.apartment.features.properties.dto.property_type.PropertyTypeCreateReq;
import com.qminh.apartment.features.properties.dto.property_type.PropertyTypeRes;
import com.qminh.apartment.features.properties.dto.property_type.PropertyTypeUpdateReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPropertyTypeService {
	PropertyTypeRes create(PropertyTypeCreateReq req);
	PropertyTypeRes get(int id);
	Page<PropertyTypeRes> list(Pageable pageable);
	Page<PropertyTypeRes> search(String keyword, Pageable pageable);
	PropertyTypeRes update(int id, PropertyTypeUpdateReq req);
	void delete(int id);
}
