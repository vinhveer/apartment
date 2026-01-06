package com.qvinh.apartment.features.files.persistence;

import com.qvinh.apartment.features.files.domain.StoredFileMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface StoredFileMetaRepository extends JpaRepository<StoredFileMeta, Long> {

	@NonNull Optional<StoredFileMeta> findBySha256(@NonNull String sha256);

	@NonNull Optional<StoredFileMeta> findByStoredName(@NonNull String storedName);

	@Query("""
		select f from StoredFileMeta f
		where (:accessLevel is null or f.accessLevel = :accessLevel)
		  and (:mimeType is null or f.mimeType like :mimeType)
		  and (
		    :search is null
		    or f.originalName like :search
		    or coalesce(f.title, '') like :search
		    or coalesce(f.tags, '') like :search
		  )
		""")
	Page<StoredFileMeta> search(
		@Param("accessLevel") String accessLevel,
		@Param("mimeType") String mimeType,
		@Param("search") String search,
		Pageable pageable
	);
}

