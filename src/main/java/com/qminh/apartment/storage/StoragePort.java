package com.qminh.apartment.storage;

import java.io.IOException;
import java.io.InputStream;

public interface StoragePort {

	record SaveResult(String relativePath, String sha256) {}

	SaveResult save(InputStream inputStream, String storedName, String subdir) throws IOException;

	InputStream load(String relativePath) throws IOException;

	boolean delete(String relativePath) throws IOException;
}


