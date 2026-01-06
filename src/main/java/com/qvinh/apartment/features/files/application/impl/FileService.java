package com.qvinh.apartment.features.files.application.impl;

import com.qvinh.apartment.features.files.application.IFileService;
import com.qvinh.apartment.features.files.domain.StoredFileMeta;
import com.qvinh.apartment.features.files.domain.StoredFileVariant;
import com.qvinh.apartment.shared.error.ErrorCode;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.features.files.persistence.StoredFileMetaRepository;
import com.qvinh.apartment.features.files.persistence.StoredFileVariantRepository;
import com.qvinh.apartment.infrastructure.storage.StoragePort;
import com.qvinh.apartment.features.files.dto.FileMetaUpdateReq;
import com.qvinh.apartment.shared.exception.AppException;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class FileService implements IFileService {

	private static final String MIME_JPEG = "image/jpeg";
	private static final String MIME_PNG = "image/png";
	private static final String MIME_WEBP = "image/webp";
	private static final String MIME_PDF = "application/pdf";
	private static final String ACCESS_PRIVATE = "PRIVATE";
	private static final String ACCESS_PUBLIC = "PUBLIC";

	private static final List<String> ALLOWED_MIME = List.of(
		MIME_JPEG, MIME_PNG, MIME_WEBP, MIME_PDF
	);
	private static final List<String> IMAGE_MIME = List.of(
		MIME_JPEG, MIME_PNG, MIME_WEBP
	);
	private static final Map<String, String> MIME_TO_EXT = Map.of(
		MIME_JPEG, "jpg",
		MIME_PNG, "png",
		MIME_WEBP, "webp",
		MIME_PDF, "pdf"
	);
	private static final List<VariantSpec> VARIANTS = List.of(
		new VariantSpec("thumb_200", 200),
		new VariantSpec("list_600", 600),
		new VariantSpec("detail_1200", 1200)
	);

	private record VariantSpec(String key, int width) {}

	private final Tika tika = new Tika();
	private final StoragePort storage;
	private final StoredFileMetaRepository fileRepo;
	private final StoredFileVariantRepository variantRepo;

	public FileService(StoragePort storage, StoredFileMetaRepository fileRepo, StoredFileVariantRepository variantRepo) {
		this.storage = storage;
		this.fileRepo = fileRepo;
		this.variantRepo = variantRepo;
	}

	@Transactional
	public StoredFileMeta upload(MultipartFile file, String accessLevel) {
		try {
			Objects.requireNonNull(file, "file must not be null");
			String normalizedAccess = normalizeAccess(accessLevel);
			// 1) Write to temp and detect MIME
			Path tempFile = Files.createTempFile("upload_", ".bin");
			File tmpAsFile = Objects.requireNonNull(tempFile.toFile());
			file.transferTo(tmpAsFile);
			String mime = tika.detect(tmpAsFile);
			if (!ALLOWED_MIME.contains(mime)) {
				Files.deleteIfExists(tempFile);
				throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					HttpStatus.UNPROCESSABLE_ENTITY,
					"Unsupported MIME type",
					Map.of("mime", mime)
				);
			}
			long sizeBytes = Files.size(tempFile);

			// 2) Compute sha256 for de-dup and decide stored name
			String sha256 = Objects.requireNonNull(computeSha256(tmpAsFile));
			var existing = fileRepo.findBySha256(sha256);
			if (existing.isPresent()) {
				Files.deleteIfExists(tempFile);
				return existing.get();
			}
			String originalName = sanitizeOriginalName(file.getOriginalFilename());
			String ext = pickExtension(originalName, mime);
			String shortHash = sha256.substring(0, 12);
			String storedName = shortHash + "." + ext;
			String subdir = buildSubdir(normalizedAccess);

			// 3) Save original to storage
			try (FileInputStream fis = new FileInputStream(tmpAsFile)) {
				storage.save(fis, storedName, subdir);
			}
			Files.deleteIfExists(tempFile);

			// 4) Persist meta
			StoredFileMeta meta = new StoredFileMeta();
			meta.setOriginalName(originalName);
			meta.setStoredName(storedName);
			meta.setExt(ext);
			meta.setMimeType(mime);
			meta.setSizeBytes(sizeBytes);
			meta.setSha256(sha256);
			meta.setAccessLevel(normalizedAccess);
			meta.setLocation("LOCAL");
			meta.setRelativePath(subdir + "/" + storedName);
			meta = fileRepo.save(meta);

			// 5) Generate variants for images
			if (IMAGE_MIME.contains(mime)) {
				generateVariants(meta, mime);
			}
			return meta;
		} catch (IOException e) {
			throw new IllegalStateException("Upload failed", e);
		}
	}

	@Transactional
	public StoredFileMeta rename(Long fileId, String originalName) {
		StoredFileMeta meta = fileRepo.findById(Objects.requireNonNull(fileId, "fileId must not be null"))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FILE_NOT_FOUND, "File not found"));
		String sanitized = sanitizeOriginalName(Objects.requireNonNull(originalName, "originalName must not be null"));
		meta.setOriginalName(sanitized);
		return Objects.requireNonNull(fileRepo.save(meta), "saved meta must not be null");
	}

	@Transactional
	public void delete(Long fileId) {
		StoredFileMeta meta = fileRepo.findById(Objects.requireNonNull(fileId))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FILE_NOT_FOUND, "File not found"));
		Long id = Objects.requireNonNull(meta.getFileId());
		// delete variants first
		var variants = variantRepo.findByFile_FileId(id);
		for (StoredFileVariant v : variants) {
			try {
				storage.delete(Objects.requireNonNull(v.getRelativePath()));
			} catch (IOException e) {
				throw new IllegalStateException("Failed to delete variant file: " + v.getRelativePath(), e);
			}
		}
		variantRepo.deleteAll(variants);
		// delete original
		try {
			storage.delete(Objects.requireNonNull(meta.getRelativePath()));
		} catch (IOException e) {
			throw new IllegalStateException("Failed to delete file: " + meta.getRelativePath(), e);
		}
		fileRepo.delete(meta);
	}

	@Transactional(readOnly = true)
	public Page<StoredFileMeta> search(
		String accessLevel,
		String mimeType,
		String search,
		LocalDate createdFrom,
		LocalDate createdTo,
		Pageable pageable
	) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		LocalDateTime from = createdFrom != null ? createdFrom.atStartOfDay() : null;
		LocalDateTime to = createdTo != null ? createdTo.atTime(23, 59, 59) : null;
		String access = accessLevel != null && !accessLevel.isBlank()
			? accessLevel.trim().toUpperCase(Locale.ROOT)
			: null;
		String mime = null;
		if (mimeType != null && !mimeType.isBlank()) {
			String mt = mimeType.trim().toLowerCase(Locale.ROOT);
			if (!mt.contains("/")) {
				// "image" -> "image/%"
				mime = mt + "/%";
			} else {
				mime = mt;
			}
		}
		String q = null;
		if (search != null && !search.isBlank()) {
			q = "%" + search.trim() + "%";
		}
		Page<StoredFileMeta> base = fileRepo.search(access, mime, q, pageable);
		if (from == null && to == null) {
			return base;
		}
		java.util.List<StoredFileMeta> filtered = base.getContent().stream()
			.filter(m -> {
				LocalDateTime ts = m.getCreatedAt();
				if (ts == null) return false;
				if (from != null && ts.isBefore(from)) return false;
				if (to != null && ts.isAfter(to)) return false;
				return true;
			})
			.toList();
		return new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
	}

	@Transactional
	@SuppressWarnings("null")
	public StoredFileMeta updateMeta(Long fileId, FileMetaUpdateReq req) {
		StoredFileMeta meta = fileRepo.findById(Objects.requireNonNull(fileId, "fileId must not be null"))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FILE_NOT_FOUND, "File not found"));
		if (req.getAltText() != null) {
			meta.setAltText(req.getAltText());
		}
		if (req.getTitle() != null) {
			meta.setTitle(req.getTitle());
		}
		if (req.getDescription() != null) {
			meta.setDescription(req.getDescription());
		}
		if (req.getTags() != null) {
			if (req.getTags().isEmpty()) {
				meta.setTags(null);
			} else {
				String joined = String.join(",", req.getTags());
				meta.setTags(joined);
			}
		}
		StoredFileMeta savedMeta = fileRepo.save(meta);
		return Objects.requireNonNull(savedMeta, "saved meta must not be null");
	}

	private void generateVariants(StoredFileMeta meta, String mime) throws IOException {
		// load original file from storage
		// We do not know absolute base; open via storage.load(relative)
		try (var is = storage.load(meta.getRelativePath())) {
			BufferedImage src = ImageIO.read(is);
			if (src == null) return;
			for (VariantSpec spec : VARIANTS) {
				BufferedImage out = scaleToWidth(src, spec.width());
				String ext = MIME_TO_EXT.getOrDefault(mime, "jpg");
				String variantStoredName = meta.getStoredName().replace("." + meta.getExt(), "") + "_" + spec.key() + "." + ext;
				String variantSubdir = parentDir(meta.getRelativePath());
				Path tmp = Files.createTempFile("variant_", "." + ext);
				ImageIO.write(out, ext, tmp.toFile());
				long size = Files.size(tmp);
				try (var fis = new FileInputStream(tmp.toFile())) {
					StoragePort.SaveResult saved = storage.save(fis, variantStoredName, variantSubdir);
					StoredFileVariant v = new StoredFileVariant();
					v.setFile(meta);
					v.setVariantKey(spec.key());
					v.setMimeType(mime);
					v.setSizeBytes(size);
					v.setWidth(out.getWidth());
					v.setHeight(out.getHeight());
					v.setRelativePath(saved.relativePath());
					variantRepo.save(v);
				} finally {
					Files.deleteIfExists(tmp);
				}
			}
		}
	}

	private static BufferedImage scaleToWidth(BufferedImage src, int targetW) throws IOException {
		if (src.getWidth() <= targetW) {
			return src;
		}
		return Thumbnails.of(src).width(targetW).keepAspectRatio(true).asBufferedImage();
	}

	private static String parentDir(String relativePath) {
		int idx = relativePath.lastIndexOf('/');
		if (idx <= 0) return "";
		return relativePath.substring(0, idx);
	}

	private static String sanitizeOriginalName(String name) {
		if (name == null) return "unknown";
		String onlyName = name.replace("\\", "/");
		int idx = onlyName.lastIndexOf('/');
		if (idx >= 0) onlyName = onlyName.substring(idx + 1);
		return onlyName.trim();
	}

	private static String pickExtension(String originalName, String mime) {
		String ext = null;
		int dot = originalName.lastIndexOf('.');
		if (dot > 0 && dot < originalName.length() - 1) {
			ext = originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
		}
		if (ext == null || ext.isBlank()) {
			ext = MIME_TO_EXT.getOrDefault(mime, "bin");
		}
		return ext;
	}

	private static String buildSubdir(String accessLevel) {
		LocalDate d = LocalDate.now();
		return (ACCESS_PRIVATE.equals(accessLevel) ? "private" : "public") + "/" +
			String.format("%04d/%02d", d.getYear(), d.getMonthValue());
	}

	private static String computeSha256(File f) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			try (var is = Files.newInputStream(f.toPath())) {
				is.transferTo(new java.io.OutputStream() {
					@Override public void write(int b) throws IOException { md.update((byte) b); }
					@Override public void write(byte[] b, int off, int len) { md.update(b, off, len); }
				});
			}
			byte[] hash = md.digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) sb.append(String.format("%02x", b));
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("SHA-256 not available", e);
		}
	}

	private static String normalizeAccess(String access) {
		if (access == null) return ACCESS_PUBLIC;
		String a = access.trim().toUpperCase(Locale.ROOT);
		return (ACCESS_PRIVATE.equals(a) ? ACCESS_PRIVATE : ACCESS_PUBLIC);
	}
}
