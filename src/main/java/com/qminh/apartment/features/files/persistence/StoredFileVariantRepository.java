package com.qminh.apartment.features.files.persistence;

import com.qminh.apartment.features.files.domain.StoredFileVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface StoredFileVariantRepository extends JpaRepository<StoredFileVariant, Long> {
	@NonNull List<StoredFileVariant> findByFile_FileId(@NonNull Long fileId);
	@NonNull Optional<StoredFileVariant> findByFile_FileIdAndVariantKey(@NonNull Long fileId, @NonNull String variantKey);
}

