package com.qminh.apartment.service;

import com.qminh.apartment.dto.property_details.PropertyDetailsCreateReq;
import com.qminh.apartment.dto.property_details.PropertyDetailsRes;
import com.qminh.apartment.dto.property_details.PropertyDetailsUpdateReq;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IPropertyDetailsService {

	@Transactional
	List<PropertyDetailsRes> create(Long propertyId, PropertyDetailsCreateReq req);

	@Transactional
	List<PropertyDetailsRes> update(Long propertyId, PropertyDetailsUpdateReq req);

	@Transactional(readOnly = true)
	List<PropertyDetailsRes> listByProperty(Long propertyId);

	@Transactional
	void deleteOne(Long propertyId, Integer detailId);

	@Transactional
	void deleteAll(Long propertyId);
}


