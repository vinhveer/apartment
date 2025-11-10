package com.qminh.apartment.repository;

import com.qminh.apartment.entity.PropertyArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import java.util.Optional;

public interface PropertyAreaRepository extends JpaRepository<PropertyArea, Integer> {
    boolean existsByAreaName(@NonNull String areaName);
    boolean existsByAreaLink(@NonNull String areaLink);
    @NonNull Optional<PropertyArea> findByAreaLink(@NonNull String areaLink);
}
