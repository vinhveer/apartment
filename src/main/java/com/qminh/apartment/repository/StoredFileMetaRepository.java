package com.qminh.apartment.repository;

import com.qminh.apartment.entity.StoredFileMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface StoredFileMetaRepository extends JpaRepository<StoredFileMeta, Long> {
	@NonNull Optional<StoredFileMeta> findBySha256(@NonNull String sha256);
	@NonNull Optional<StoredFileMeta> findByStoredName(@NonNull String storedName);
}


