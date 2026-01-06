package com.qvinh.apartment.features.properties.persistence;

import com.qvinh.apartment.features.properties.domain.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property>, PropertyRepositoryCustom {
}

