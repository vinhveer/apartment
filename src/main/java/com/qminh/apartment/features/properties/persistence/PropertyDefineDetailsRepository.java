package com.qminh.apartment.features.properties.persistence;

import com.qminh.apartment.features.properties.domain.PropertyDefineDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface PropertyDefineDetailsRepository extends JpaRepository<PropertyDefineDetails, Integer> {
	boolean existsByDetailName(@NonNull String detailName);
}

