package com.qminh.apartment.repository;

import com.qminh.apartment.entity.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface PropertyTypeRepository extends JpaRepository<PropertyType, Integer> {
	boolean existsByTypeName(@NonNull String typeName);
}


