package com.qminh.apartment.controller;

import com.qminh.apartment.dto.ApiResponse;
import com.qminh.apartment.dto.property.PropertyCreateReq;
import com.qminh.apartment.dto.property.PropertyRes;
import com.qminh.apartment.dto.property.PropertySearchReq;
import com.qminh.apartment.dto.property.PropertyUpdateReq;
import com.qminh.apartment.service.IPropertyService;
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
	public ResponseEntity<ApiResponse<PropertyRes>> get(@PathVariable Long id) {
		PropertyRes res = service.get(id);
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
	public ResponseEntity<ApiResponse<Page<PropertyRes>>> search(
		@RequestBody PropertySearchReq req,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) String sort
	) {
		Pageable pageable = buildPageable(page, size, sort);
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


