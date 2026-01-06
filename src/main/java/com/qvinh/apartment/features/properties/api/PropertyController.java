package com.qvinh.apartment.features.properties.api;

import com.qvinh.apartment.shared.api.ApiResponse;
import com.qvinh.apartment.features.properties.dto.property.PropertyCreateReq;
import com.qvinh.apartment.features.properties.dto.property.PropertyRes;
import com.qvinh.apartment.features.properties.dto.property.PropertySearchReq;
import com.qvinh.apartment.features.properties.dto.property.PropertySelectRes;
import com.qvinh.apartment.features.properties.dto.property.PropertyUpdateReq;
import com.qvinh.apartment.features.properties.application.IPropertyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

	private final IPropertyService service;

	public PropertyController(IPropertyService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<PropertyRes>> create(@Valid @RequestBody PropertyCreateReq req) {
		PropertyRes res = service.create(req);
		return ResponseEntity.ok(ApiResponse.ok("Create property successfully", res));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<PropertySelectRes>> get(@PathVariable Long id) {
		PropertySelectRes res = service.getFull(id);
		return ResponseEntity.ok(ApiResponse.ok("Property detail", res));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Page<PropertyRes>>> list(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		Page<PropertyRes> res = service.list(pageable);
		var meta = new ApiResponse.Meta(page, size, res.getTotalElements());
		return ResponseEntity.ok(ApiResponse.ok("Property list", res, meta));
	}

	@PostMapping("/search")
	public ResponseEntity<ApiResponse<? extends Page<?>>> search(
		@RequestBody PropertySearchReq req,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) String sort,
		@RequestParam(defaultValue = "list") String mode
	) {
		Pageable pageable = buildPageable(page, size, sort);
		if ("select".equalsIgnoreCase(mode)) {
			Page<PropertySelectRes> res = service.searchFull(req, pageable);
			var meta = new ApiResponse.Meta(page, size, res.getTotalElements());
			return ResponseEntity.ok(ApiResponse.ok("Property search result (select)", res, meta));
		}
		Page<PropertyRes> res = service.search(req, pageable);
		var meta = new ApiResponse.Meta(page, size, res.getTotalElements());
		return ResponseEntity.ok(ApiResponse.ok("Property search result", res, meta));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<PropertyRes>> update(
		@PathVariable Long id,
		@Valid @RequestBody PropertyUpdateReq req
	) {
		PropertyRes res = service.update(id, req);
		return ResponseEntity.ok(ApiResponse.ok("Update property successfully", res));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.ok(ApiResponse.ok("Delete property successfully", null));
	}

	private Pageable buildPageable(int page, int size, String sort) {
		Sort sortObj;
		if (sort != null && !sort.trim().isEmpty()) {
			String[] parts = sort.split(",");
			if (parts.length == 2) {
				String field = parts[0].trim();
				String direction = parts[1].trim().toUpperCase();
				Sort.Direction dir = "DESC".equals(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
				sortObj = Sort.by(dir, field);
			} else {
				sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
			}
		} else {
			sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
		}
		return PageRequest.of(page, size, sortObj);
	}
}
