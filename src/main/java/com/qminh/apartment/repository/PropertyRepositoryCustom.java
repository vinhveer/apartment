package com.qminh.apartment.repository;

import com.qminh.apartment.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface PropertyRepositoryCustom {
	Page<Property> findAllWithRelations(Specification<Property> spec, Pageable pageable);
	Property findByIdWithRelations(Long id);
}
