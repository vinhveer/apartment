package com.qminh.apartment.service;

import com.qminh.apartment.entity.StoredFileMeta;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
	StoredFileMeta upload(MultipartFile file, String accessLevel);
}


