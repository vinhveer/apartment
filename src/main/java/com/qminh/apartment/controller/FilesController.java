package com.qminh.apartment.controller;

import com.qminh.apartment.dto.ApiResponse;
import com.qminh.apartment.entity.StoredFileMeta;
import com.qminh.apartment.entity.StoredFileVariant;
import com.qminh.apartment.repository.StoredFileMetaRepository;
import com.qminh.apartment.repository.StoredFileVariantRepository;
import com.qminh.apartment.service.IFileService;
import com.qminh.apartment.storage.StoragePort;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FilesController {

	private final IFileService fileService;
	private final StoredFileMetaRepository fileRepo;
	private final StoredFileVariantRepository variantRepo;
	private final StoragePort storage;

	public FilesController(IFileService fileService, StoredFileMetaRepository fileRepo,
	                       StoredFileVariantRepository variantRepo, StoragePort storage) {
		this.fileService = fileService;
		this.fileRepo = fileRepo;
		this.variantRepo = variantRepo;
		this.storage = storage;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<Map<String, Object>>> upload(
		@RequestPart("file") MultipartFile file,
		@RequestParam(name = "accessLevel", defaultValue = "PUBLIC") String accessLevel
	) {
		StoredFileMeta meta = fileService.upload(file, accessLevel);
		var variants = variantRepo.findByFile_FileId(Objects.requireNonNull(meta.getFileId()))
			.stream()
			.collect(Collectors.toMap(StoredFileVariant::getVariantKey, StoredFileVariant::getRelativePath));
		Map<String, Object> data = Map.of(
			"fileId", meta.getFileId(),
			"originalName", meta.getOriginalName(),
			"mimeType", meta.getMimeType(),
			"sizeBytes", meta.getSizeBytes(),
			"accessLevel", meta.getAccessLevel(),
			"relativePath", meta.getRelativePath(),
			"variants", variants
		);
		return ResponseEntity.ok(ApiResponse.ok("Upload file successfully", data));
	}

	@GetMapping("/{id}")
	public ResponseEntity<InputStreamResource> getPrivate(@PathVariable Long id) throws IOException {
		StoredFileMeta meta = fileRepo.findById(Objects.requireNonNull(id)).orElseThrow();
		var is = storage.load(meta.getRelativePath());
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + meta.getStoredName() + "\"")
			.contentType(MediaType.parseMediaType(Objects.requireNonNull(meta.getMimeType())))
			.body(new InputStreamResource(Objects.requireNonNull(is)));
	}

	@GetMapping("/{id}/variant/{key}")
	public ResponseEntity<InputStreamResource> getPrivateVariant(@PathVariable Long id, @PathVariable String key) throws IOException {
		StoredFileMeta meta = fileRepo.findById(Objects.requireNonNull(id)).orElseThrow();
		StoredFileVariant variant = variantRepo.findByFile_FileIdAndVariantKey(Objects.requireNonNull(id), Objects.requireNonNull(key)).orElseThrow();
		var is = storage.load(variant.getRelativePath());
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + variant.getVariantKey() + "-" + meta.getStoredName() + "\"")
			.contentType(MediaType.parseMediaType(Objects.requireNonNull(variant.getMimeType())))
			.body(new InputStreamResource(Objects.requireNonNull(is)));
	}
}


