package com.qvinh.apartment.features.files.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stored_file_variant")
public class StoredFileVariant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "variant_id")
	private Long variantId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "file_id", nullable = false)
	private StoredFileMeta file;

	@Column(name = "variant_key", nullable = false, length = 32)
	private String variantKey;

	@Column(name = "mime_type", nullable = false, length = 255)
	private String mimeType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(name = "width")
	private Integer width;

	@Column(name = "height")
	private Integer height;

	@Column(name = "relative_path", nullable = false, columnDefinition = "text")
	private String relativePath;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}
}

