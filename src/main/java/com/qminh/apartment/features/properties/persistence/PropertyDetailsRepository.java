package com.qminh.apartment.features.properties.persistence;

import com.qminh.apartment.features.properties.domain.PropertyDetails;
import com.qminh.apartment.features.properties.domain.PropertyDetailsId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PropertyDetailsRepository extends JpaRepository<PropertyDetails, PropertyDetailsId> {

	List<PropertyDetails> findByProperty_PropertyId(Long propertyId);

	Optional<PropertyDetails> findById_DetailIdAndId_PropertyId(Integer detailId, Long propertyId);

	long countById_PropertyIdAndId_DetailIdIn(Long propertyId, Collection<Integer> detailIds);

	void deleteById_DetailIdAndId_PropertyId(Integer detailId, Long propertyId);

	void deleteById_PropertyId(Long propertyId);
}

