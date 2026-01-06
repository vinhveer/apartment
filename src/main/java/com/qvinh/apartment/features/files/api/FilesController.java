package com.qvinh.apartment.features.files.api;

import com.qvinh.apartment.shared.api.ApiResponse;
import com.qvinh.apartment.features.files.constants.FilesMessages;
import com.qvinh.apartment.features.files.dto.FileMetaUpdateReq;
import com.qvinh.apartment.features.files.dto.FileRenameReq;
import com.qvinh.apartment.features.files.domain.StoredFileMeta;
import com.qvinh.apartment.features.files.domain.StoredFileVariant;
import com.qvinh.apartment.shared.error.ErrorCode;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.features.files.persistence.StoredFileMetaRepository;
import com.qvinh.apartment.features.files.persistence.StoredFileVariantRepository;
import com.qvinh.apartment.features.files.application.IFileService;
import com.qvinh.apartment.infrastructure.storage.StoragePort;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping(FilesController.BASE_PATH)
public class FilesController {

	public static final String BASE_PATH = "/api/files";
	public static final String BASE_PATH_ALL = BASE_PATH + "/**";

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
		Map<String, Object> data = buildMeta(meta);
		return ResponseEntity.ok(ApiResponse.ok("Upload file successfully", data));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<Map<String, Object>>> getMeta(@PathVariable Long id) {
		StoredFileMeta meta = fileRepo.findById(Objects.requireNonNull(id))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FILE_NOT_FOUND, FilesMessages.FILE_NOT_FOUND));
		Map<String, Object> data = buildMeta(meta);
		return ResponseEntity.ok(ApiResponse.ok("Get file successfully", data));
	}

	@GetMapping("/{id}/variant/{key}")
	public ResponseEntity<InputStreamResource> getPrivateVariant(@PathVariable Long id, @PathVariable String key) throws IOException {
		StoredFileMeta meta = fileRepo.findById(Objects.requireNonNull(id))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FILE_NOT_FOUND, FilesMessages.FILE_NOT_FOUND));
		StoredFileVariant variant = variantRepo.findByFile_FileIdAndVariantKey(
				Objects.requireNonNull(id),
				Objects.requireNonNull(key)
			)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FILE_VARIANT_NOT_FOUND, FilesMessages.FILE_VARIANT_NOT_FOUND));
		var is = storage.load(variant.getRelativePath());
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + variant.getVariantKey() + "-" + meta.getStoredName() + "\"")
			.contentType(MediaType.parseMediaType(Objects.requireNonNull(variant.getMimeType())))
			.body(new InputStreamResource(Objects.requireNonNull(is)));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Map<String, Object>>> list(
		@RequestParam(name = "accessLevel", required = false) String accessLevel,
		@RequestParam(name = "search", required = false) String search,
		@RequestParam(name = "mimeType", required = false) String mimeType,
		@RequestParam(name = "createdFrom", required = false) java.time.LocalDate createdFrom,
		@RequestParam(name = "createdTo", required = false) java.time.LocalDate createdTo,
		@RequestParam(name = "page", defaultValue = "1") int page,
		@RequestParam(name = "pageSize", defaultValue = "20") int pageSize
	) {
		int p = Math.max(page, 1) - 1;
		int size = Math.max(1, Math.min(pageSize, 100));
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
			p,
			size,
			org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
		);
		var pageRes = fileService.search(accessLevel, mimeType, search, createdFrom, createdTo, pageable);
		List<Map<String, Object>> items = new ArrayList<>();
		for (StoredFileMeta meta : pageRes.getContent()) {
			items.add(buildMeta(meta));
		}
		long totalItems = pageRes.getTotalElements();
		long totalPages = pageRes.getTotalPages();
		Map<String, Object> data = new java.util.HashMap<>();
		data.put("items", items);
		data.put("page", page);
		data.put("pageSize", size);
		data.put("totalItems", totalItems);
		data.put("totalPages", totalPages);
		return ResponseEntity.ok(ApiResponse.ok("Get files successfully", data));
	}

	@PutMapping("/{id}/name")
	public ResponseEntity<ApiResponse<Map<String, Object>>> rename(
		@PathVariable Long id,
		@RequestBody FileRenameReq req
	) {
		StoredFileMeta meta = fileService.rename(id, Objects.requireNonNull(req).getOriginalName());
		Map<String, Object> data = buildMeta(meta);
		return ResponseEntity.ok(ApiResponse.ok("Rename file successfully", data));
	}

	@PutMapping("/{id}/meta")
	public ResponseEntity<ApiResponse<Map<String, Object>>> updateMeta(
		@PathVariable Long id,
		@RequestBody FileMetaUpdateReq req
	) {
		StoredFileMeta meta = fileService.updateMeta(id, Objects.requireNonNull(req));
		Map<String, Object> data = buildMeta(meta);
		return ResponseEntity.ok(ApiResponse.ok("Update file metadata successfully", data));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		fileService.delete(id);
		return ResponseEntity.ok(ApiResponse.ok("Delete file successfully", null));
	}

	private Map<String, Object> buildMeta(StoredFileMeta meta) {
		var variants = variantRepo.findByFile_FileId(Objects.requireNonNull(meta.getFileId()))
			.stream()
			.collect(Collectors.toMap(StoredFileVariant::getVariantKey, StoredFileVariant::getRelativePath));
		List<String> tags = new ArrayList<>();
		if (meta.getTags() != null && !meta.getTags().isBlank()) {
			tags = Arrays.stream(meta.getTags().split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toList();
		}
		java.util.Map<String, Object> data = new java.util.HashMap<>();
		data.put("fileId", meta.getFileId());
		data.put("originalName", meta.getOriginalName());
		data.put("mimeType", meta.getMimeType());
		data.put("sizeBytes", meta.getSizeBytes());
		data.put("accessLevel", meta.getAccessLevel());
		data.put("relativePath", meta.getRelativePath());
		data.put("variants", variants);
		data.put("altText", meta.getAltText());
		data.put("title", meta.getTitle());
		data.put("description", meta.getDescription());
		data.put("tags", tags);
		data.put("createdAt", meta.getCreatedAt());
		data.put("updatedAt", meta.getUpdatedAt());
		return data;
	}
}
