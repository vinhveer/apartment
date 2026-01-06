package com.qvinh.apartment.features.properties.application;

import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsCreateReq;
import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsRes;
import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsUpdateReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPropertyDefineDetailsService {
	PropertyDefineDetailsRes create(PropertyDefineDetailsCreateReq req);
	PropertyDefineDetailsRes get(int id);
	Page<PropertyDefineDetailsRes> list(Pageable pageable);
	PropertyDefineDetailsRes update(int id, PropertyDefineDetailsUpdateReq req);
	void delete(int id);
}
