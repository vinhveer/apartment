package com.qminh.apartment.repository;

import com.qminh.apartment.entity.PropertyDefineDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface PropertyDefineDetailsRepository extends JpaRepository<PropertyDefineDetails, Integer> {
	boolean existsByDetailName(@NonNull String detailName);
}


