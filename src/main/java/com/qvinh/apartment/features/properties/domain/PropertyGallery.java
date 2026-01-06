package com.qvinh.apartment.features.properties.domain;

import com.qvinh.apartment.features.files.domain.StoredFileMeta;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "property_gallery")
public class PropertyGallery {

	@EmbeddedId
	private PropertyGalleryId id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("propertyId")
	@JoinColumn(name = "property_id", nullable = false)
	private Property property;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("fileId")
	@JoinColumn(name = "file_id", nullable = false)
	private StoredFileMeta file;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}

