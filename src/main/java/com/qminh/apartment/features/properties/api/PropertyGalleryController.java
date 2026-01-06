package com.qminh.apartment.features.properties.api;

import com.qminh.apartment.shared.api.ApiResponse;
import com.qminh.apartment.features.properties.dto.property_gallery.PropertyGalleryCreateReq;
import com.qminh.apartment.features.properties.dto.property_gallery.PropertyGalleryRes;
import com.qminh.apartment.features.properties.application.IPropertyGalleryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/properties/{propertyId}/gallery")
public class PropertyGalleryController {

	private final IPropertyGalleryService service;

	public PropertyGalleryController(IPropertyGalleryService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<PropertyGalleryRes>> addFileIntoGallery(
		@PathVariable Long propertyId,
		@Valid @RequestBody PropertyGalleryCreateReq req
	) {
		PropertyGalleryRes res = service.addFileIntoGallery(propertyId, req.getFileId());
		return ResponseEntity.ok(ApiResponse.ok("Add file into gallery successfully", res));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<PropertyGalleryRes>>> getFileByPropertiesId(
		@PathVariable Long propertyId
	) {
		List<PropertyGalleryRes> res = service.getFileByPropertiesId(propertyId);
		return ResponseEntity.ok(ApiResponse.ok("Property gallery files", res));
	}

	@DeleteMapping("/{fileId}")
	public ResponseEntity<ApiResponse<Void>> deleteFileIntoGallery(
		@PathVariable Long propertyId,
		@PathVariable Long fileId
	) {
		service.deleteFileIntoGallery(propertyId, fileId);
		return ResponseEntity.ok(ApiResponse.ok("Delete file from gallery successfully", null));
	}
}
