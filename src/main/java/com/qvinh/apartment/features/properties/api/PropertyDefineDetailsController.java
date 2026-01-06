package com.qvinh.apartment.features.properties.api;

import com.qvinh.apartment.shared.api.ApiResponse;
import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsCreateReq;
import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsRes;
import com.qvinh.apartment.features.properties.dto.property_define_details.PropertyDefineDetailsUpdateReq;
import com.qvinh.apartment.features.properties.application.IPropertyDefineDetailsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(PropertyDefineDetailsController.BASE_PATH)
public class PropertyDefineDetailsController {

	public static final String BASE_PATH = "/api/property-define-details";
	public static final String BASE_PATH_ALL = BASE_PATH + "/**";

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
