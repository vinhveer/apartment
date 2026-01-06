package com.qvinh.apartment.features.properties.api;

import com.qvinh.apartment.shared.api.ApiResponse;
import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsCreateReq;
import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsRes;
import com.qvinh.apartment.features.properties.dto.property_details.PropertyDetailsUpdateReq;
import com.qvinh.apartment.features.properties.application.IPropertyDetailsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties/{propertyId}/details")
public class PropertyDetailsController {

	private final IPropertyDetailsService service;

	public PropertyDetailsController(IPropertyDetailsService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<List<PropertyDetailsRes>>> create(
		@PathVariable Long propertyId,
		@Valid @RequestBody PropertyDetailsCreateReq req
	) {
		List<PropertyDetailsRes> res = service.create(propertyId, req);
		return ResponseEntity.ok(ApiResponse.ok("Create property details successfully", res));
	}

	@PatchMapping
	public ResponseEntity<ApiResponse<List<PropertyDetailsRes>>> update(
		@PathVariable Long propertyId,
		@Valid @RequestBody PropertyDetailsUpdateReq req
	) {
		List<PropertyDetailsRes> res = service.update(propertyId, req);
		return ResponseEntity.ok(ApiResponse.ok("Update property details successfully", res));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<PropertyDetailsRes>>> list(
		@PathVariable Long propertyId
	) {
		List<PropertyDetailsRes> res = service.listByProperty(propertyId);
		return ResponseEntity.ok(ApiResponse.ok("Property details", res));
	}

	@DeleteMapping("/{detailId}")
	public ResponseEntity<ApiResponse<Void>> deleteOne(
		@PathVariable Long propertyId,
		@PathVariable Integer detailId
	) {
		service.deleteOne(propertyId, detailId);
		return ResponseEntity.ok(ApiResponse.ok("Delete property detail successfully", null));
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> deleteAll(@PathVariable Long propertyId) {
		service.deleteAll(propertyId);
		return ResponseEntity.ok(ApiResponse.ok("Delete all property details successfully", null));
	}
}
