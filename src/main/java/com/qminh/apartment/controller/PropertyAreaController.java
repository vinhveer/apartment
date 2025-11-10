package com.qminh.apartment.controller;

import com.qminh.apartment.dto.ApiResponse;
import com.qminh.apartment.dto.area.PropertyAreaCreateReq;
import com.qminh.apartment.dto.area.PropertyAreaRes;
import com.qminh.apartment.dto.area.PropertyAreaUpdateReq;
import com.qminh.apartment.service.IPropertyAreaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/areas")
public class PropertyAreaController {

	private final IPropertyAreaService service;

	public PropertyAreaController(IPropertyAreaService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<PropertyAreaRes>> create(@Valid @RequestBody PropertyAreaCreateReq req) {
		PropertyAreaRes res = service.create(req);
		return ResponseEntity.ok(ApiResponse.ok("Create property area successfully", res));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<PropertyAreaRes>> get(@PathVariable Integer id) {
		PropertyAreaRes res = service.get(id);
		return ResponseEntity.ok(ApiResponse.ok("Property area detail", res));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Page<PropertyAreaRes>>> list(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		Page<PropertyAreaRes> res = service.list(pageable);
		var meta = new ApiResponse.Meta(page, size, res.getTotalElements());
		return ResponseEntity.ok(ApiResponse.ok("Property area list", res, meta));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<PropertyAreaRes>> update(
		@PathVariable Integer id,
		@Valid @RequestBody PropertyAreaUpdateReq req
	) {
		PropertyAreaRes res = service.update(id, req);
		return ResponseEntity.ok(ApiResponse.ok("Update property area successfully", res));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
		service.delete(id);
		return ResponseEntity.ok(ApiResponse.ok("Delete property area successfully", null));
	}
}


