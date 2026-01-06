package com.qvinh.apartment.features.properties.api;

import com.qvinh.apartment.shared.api.ApiResponse;
import com.qvinh.apartment.features.properties.dto.property_type.PropertyTypeCreateReq;
import com.qvinh.apartment.features.properties.dto.property_type.PropertyTypeRes;
import com.qvinh.apartment.features.properties.dto.property_type.PropertyTypeUpdateReq;
import com.qvinh.apartment.features.properties.application.IPropertyTypeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(PropertyTypeController.BASE_PATH)
public class PropertyTypeController {

	public static final String BASE_PATH = "/api/property-types";
	public static final String BASE_PATH_ALL = BASE_PATH + "/**";

	private final IPropertyTypeService service;

	public PropertyTypeController(IPropertyTypeService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<PropertyTypeRes>> create(@Valid @RequestBody PropertyTypeCreateReq req) {
		PropertyTypeRes res = service.create(req);
		return ResponseEntity.ok(ApiResponse.ok("Create property type successfully", res));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<PropertyTypeRes>> get(@PathVariable Integer id) {
		PropertyTypeRes res = service.get(id);
		return ResponseEntity.ok(ApiResponse.ok("Property type detail", res));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Page<PropertyTypeRes>>> list(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) String keyword
	) {
		Pageable pageable = PageRequest.of(page, size);
		Page<PropertyTypeRes> res;
		String message;
		if (keyword != null && !keyword.trim().isEmpty()) {
			res = service.search(keyword, pageable);
			message = "Property type search result";
		} else {
			res = service.list(pageable);
			message = "Property type list";
		}
		var meta = new ApiResponse.Meta(page, size, res.getTotalElements());
		return ResponseEntity.ok(ApiResponse.ok(message, res, meta));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<PropertyTypeRes>> update(
		@PathVariable Integer id,
		@Valid @RequestBody PropertyTypeUpdateReq req
	) {
		PropertyTypeRes res = service.update(id, req);
		return ResponseEntity.ok(ApiResponse.ok("Update property type successfully", res));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
		service.delete(id);
		return ResponseEntity.ok(ApiResponse.ok("Delete property type successfully", null));
	}
}
