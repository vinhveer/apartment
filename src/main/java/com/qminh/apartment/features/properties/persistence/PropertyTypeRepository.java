package com.qminh.apartment.features.properties.persistence;

import com.qminh.apartment.features.properties.domain.PropertyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface PropertyTypeRepository extends JpaRepository<PropertyType, Integer> {
	boolean existsByTypeName(@NonNull String typeName);
	Page<PropertyType> findByTypeNameContainingIgnoreCase(@NonNull String typeName, Pageable pageable);
}

