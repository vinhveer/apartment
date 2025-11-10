package com.qminh.apartment.controller;

import com.qminh.apartment.dto.ApiResponse;
import com.qminh.apartment.dto.property_define_details.PropertyDefineDetailsCreateReq;
import com.qminh.apartment.dto.property_define_details.PropertyDefineDetailsRes;
import com.qminh.apartment.dto.property_define_details.PropertyDefineDetailsUpdateReq;
import com.qminh.apartment.service.IPropertyDefineDetailsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/property-define-details")
public class PropertyDefineDetailsController {

	private final IPropertyDefineDetailsService service;

	public PropertyDefineDetailsController(IPropertyDefineDetailsService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<PropertyDefineDetailsRes>> create(@Valid @RequestBody PropertyDefineDetailsCreateReq req) {
		PropertyDefineDetailsRes res = service.create(req);
		return ResponseEntity.ok(ApiResponse.ok("Create property define details successfully", res));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<PropertyDefineDetailsRes>> get(@PathVariable Integer id) {
		PropertyDefineDetailsRes res = service.get(id);
		return ResponseEntity.ok(ApiResponse.ok("Property define details detail", res));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Page<PropertyDefineDetailsRes>>> list(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		Page<PropertyDefineDetailsRes> res = service.list(pageable);
		var meta = new ApiResponse.Meta(page, size, res.getTotalElements());
		return ResponseEntity.ok(ApiResponse.ok("Property define details list", res, meta));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<PropertyDefineDetailsRes>> update(
		@PathVariable Integer id,
		@Valid @RequestBody PropertyDefineDetailsUpdateReq req
	) {
		PropertyDefineDetailsRes res = service.update(id, req);
		return ResponseEntity.ok(ApiResponse.ok("Update property define details successfully", res));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
		service.delete(id);
		return ResponseEntity.ok(ApiResponse.ok("Delete property define details successfully", null));
	}
}


