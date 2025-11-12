package com.qminh.apartment.storage;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Component
public class LocalStoragePort implements StoragePort {

	private final Path basePath;

	public LocalStoragePort(@Value("${app.upload.base-path:./uploads}") String basePath) {
		this.basePath = Path.of(Objects.requireNonNull(basePath).trim()).toAbsolutePath().normalize();
	}

	@Override
	public SaveResult save(InputStream inputStream, String storedName, String subdir) throws IOException {
		Objects.requireNonNull(inputStream, "inputStream must not be null");
		Objects.requireNonNull(storedName, "storedName must not be null");
		Objects.requireNonNull(subdir, "subdir must not be null");

		Path tmpDir = basePath.resolve(".tmp");
		Files.createDirectories(tmpDir);
		Path tmpFile = Files.createTempFile(tmpDir, "upload_", ".bin");

		MessageDigest digest = newMessageDigestSha256();
		try (DigestInputStream dis = new DigestInputStream(inputStream, digest);
		     FileOutputStream fos = new FileOutputStream(tmpFile.toFile())) {
			dis.transferTo(fos);
		}
		String sha256 = Hex.encodeHexString(digest.digest());

		Path targetDir = basePath.resolve(subdir).normalize();
		if (!targetDir.startsWith(basePath)) {
			throw new IOException("Invalid subdir path");
		}
		Files.createDirectories(targetDir);
		Path targetFile = targetDir.resolve(storedName).normalize();
		Files.move(tmpFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

		String relativePath = basePath.relativize(targetFile).toString().replace(File.separatorChar, '/');
		return new SaveResult(relativePath, sha256);
	}

	@Override
	public InputStream load(String relativePath) throws IOException {
		Objects.requireNonNull(relativePath, "relativePath must not be null");
		Path path = basePath.resolve(relativePath).normalize();
		if (!path.startsWith(basePath)) {
			throw new IOException("Invalid path");
		}
		return new FileInputStream(path.toFile());
	}

	@Override
	public boolean delete(String relativePath) throws IOException {
		Objects.requireNonNull(relativePath, "relativePath must not be null");
		Path path = basePath.resolve(relativePath).normalize();
		if (!path.startsWith(basePath)) {
			throw new IOException("Invalid path");
		}
		return Files.deleteIfExists(path);
	}

	private static MessageDigest newMessageDigestSha256() throws IOException {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("SHA-256 not available", e);
		}
	}
}


