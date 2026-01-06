package com.qvinh.apartment.features.accounts.persistence;

import com.qvinh.apartment.features.accounts.domain.PropertySaleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface PropertySaleInfoRepository extends JpaRepository<PropertySaleInfo, Long> {
	@NonNull Optional<PropertySaleInfo> findByUserId(@NonNull Long userId);
	void deleteByUserId(@NonNull Long userId);
}

