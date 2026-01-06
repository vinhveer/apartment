package com.qvinh.apartment.features.properties.application;

import com.qvinh.apartment.features.properties.dto.property.PropertyCreateReq;
import com.qvinh.apartment.features.properties.dto.property.PropertyRes;
import com.qvinh.apartment.features.properties.dto.property.PropertySearchReq;
import com.qvinh.apartment.features.properties.dto.property.PropertySelectRes;
import com.qvinh.apartment.features.properties.dto.property.PropertyUpdateReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPropertyService {
	PropertyRes create(PropertyCreateReq req);
	PropertyRes get(long id);
	PropertySelectRes getFull(long id);
	Page<PropertyRes> list(Pageable pageable);
	Page<PropertyRes> search(PropertySearchReq req, Pageable pageable);
	Page<PropertySelectRes> searchFull(PropertySearchReq req, Pageable pageable);
	PropertyRes update(long id, PropertyUpdateReq req);
	void delete(long id);
}
