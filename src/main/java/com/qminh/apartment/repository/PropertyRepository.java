package com.qminh.apartment.repository;

import com.qminh.apartment.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}


