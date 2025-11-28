package com.qminh.apartment.service;

import com.qminh.apartment.entity.StoredFileMeta;
import com.qminh.apartment.dto.file.FileMetaUpdateReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
	StoredFileMeta upload(MultipartFile file, String accessLevel);

	StoredFileMeta rename(Long fileId, String originalName);

	void delete(Long fileId);

	Page<StoredFileMeta> search(
		String accessLevel,
		String mimeType,
		String search,
		java.time.LocalDate createdFrom,
		java.time.LocalDate createdTo,
		Pageable pageable
	);

	StoredFileMeta updateMeta(Long fileId, FileMetaUpdateReq req);
}


