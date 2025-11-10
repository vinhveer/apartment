package com.qminh.apartment.repository;

import com.qminh.apartment.entity.PropertyArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyAreaRepository extends JpaRepository<PropertyArea, Integer> {
	boolean existsByAreaName(String areaName);
	boolean existsByAreaLink(String areaLink);
	Optional<PropertyArea> findByAreaLink(String areaLink);
}


