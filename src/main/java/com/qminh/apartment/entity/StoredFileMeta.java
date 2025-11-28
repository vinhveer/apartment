package com.qminh.apartment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stored_file")
public class StoredFileMeta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "file_id")
	private Long fileId;

	@Column(name = "original_name", nullable = false, length = 255)
	private String originalName;

	@Column(name = "stored_name", nullable = false, length = 255)
	private String storedName;

	@Column(name = "ext", length = 16)
	private String ext;

	@Column(name = "mime_type", nullable = false, length = 255)
	private String mimeType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(name = "sha256", nullable = false, length = 64)
	private String sha256;

	@Column(name = "access_level", nullable = false, length = 16)
	private String accessLevel; // PUBLIC or PRIVATE

	@Column(name = "location", nullable = false, length = 16)
	private String location; // LOCAL (ready for future S3)

	@Column(name = "relative_path", nullable = false, columnDefinition = "text")
	private String relativePath;

	@Column(name = "alt_text", length = 255)
	private String altText;

	@Column(name = "title", length = 255)
	private String title;

	@Column(name = "description", columnDefinition = "text")
	private String description;

	@Column(name = "tags", columnDefinition = "text")
	private String tags;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now();
	}
}


