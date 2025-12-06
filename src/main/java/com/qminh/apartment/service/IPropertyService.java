package com.qminh.apartment.service;

import com.qminh.apartment.dto.property.PropertyCreateReq;
import com.qminh.apartment.dto.property.PropertyRes;
import com.qminh.apartment.dto.property.PropertySearchReq;
import com.qminh.apartment.dto.property.PropertyUpdateReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPropertyService {
	PropertyRes create(PropertyCreateReq req);
	PropertyRes get(long id);
	Page<PropertyRes> list(Pageable pageable);
	Page<PropertyRes> search(PropertySearchReq req, Pageable pageable);
	PropertyRes update(long id, PropertyUpdateReq req);
	void delete(long id);
}


