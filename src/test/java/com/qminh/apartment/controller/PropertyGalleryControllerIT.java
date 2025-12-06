package com.qminh.apartment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qminh.apartment.dto.auth.LoginReq;
import com.qminh.apartment.dto.property.PropertyCreateReq;
import com.qminh.apartment.entity.PropertyArea;
import com.qminh.apartment.entity.PropertySaleInfo;
import com.qminh.apartment.entity.PropertyType;
import com.qminh.apartment.entity.Role;
import com.qminh.apartment.entity.StoredFileMeta;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.repository.PropertyAreaRepository;
import com.qminh.apartment.repository.PropertyTypeRepository;
import com.qminh.apartment.repository.PropertySaleInfoRepository;
import com.qminh.apartment.repository.RoleRepository;
import com.qminh.apartment.repository.StoredFileMetaRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
	"app.upload.base-path=./build/test-uploads"
})
@Transactional
class PropertyGalleryControllerIT extends PostgresTestContainer {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper mapper;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private PropertyTypeRepository typeRepository;
	@Autowired private PropertyAreaRepository areaRepository;
	@Autowired private PropertySaleInfoRepository saleInfoRepository;
	@Autowired private StoredFileMetaRepository storedFileMetaRepository;

	private static final String USERNAME_SALE = "saleGallery";
	private static final String AUTH_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private Long saleUserId;

	@BeforeEach
	void setup() {
		// Ensure role for login user
		Role userRole = roleRepository.findByRoleName("USER").orElseGet(() -> {
			Role r = new Role();
			r.setRoleName("USER");
			return roleRepository.saveAndFlush(r);
		});
		if (userRepository.findByUsername(USERNAME_SALE).isEmpty()) {
			User loginUser = new User();
			loginUser.setUsername(USERNAME_SALE);
			loginUser.setEmail("saleGallery@example.com");
			loginUser.setPassword(passwordEncoder.encode("123456"));
			loginUser.setRole(userRole);
			userRepository.saveAndFlush(loginUser);
		}

		// Create dedicated SALE user with sale info for property
		Role saleRole = roleRepository.findByRoleName("SALE").orElseGet(() -> {
			Role r = new Role();
			r.setRoleName("SALE");
			return roleRepository.saveAndFlush(r);
		});
		User saleUser = new User();
		saleUser.setUsername("saleGalleryProp_" + System.nanoTime());
		saleUser.setEmail("saleGalleryProp_" + System.nanoTime() + "@example.com");
		saleUser.setPassword("x");
		saleUser.setRole(saleRole);
		saleUser = userRepository.saveAndFlush(saleUser);
		User managedSaleUser = userRepository.findById(Objects.requireNonNull(saleUser.getId()))
			.orElseThrow();
		PropertySaleInfo info = new PropertySaleInfo();
		info.setUser(managedSaleUser);
		info.setFullName("Sale Gallery");
		info.setPhone("0900");
		saleInfoRepository.saveAndFlush(info);
		saleUserId = Objects.requireNonNull(managedSaleUser.getId());
	}

	private String login() throws Exception {
		LoginReq req = new LoginReq();
		req.setUsername(USERNAME_SALE);
		req.setPassword("123456");
		var res = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andReturn().getResponse().getContentAsString();
		return mapper.readTree(res).path("data").path("accessToken").asText();
	}

	private Long prepareProperty(String accessToken) {
		PropertyType type = new PropertyType();
		type.setTypeName("TypeG");
		type = typeRepository.saveAndFlush(type);

		PropertyArea area = new PropertyArea();
		area.setAreaName("AreaG");
		area.setAreaLink("area-g");
		area = areaRepository.saveAndFlush(area);

		PropertySaleInfo info = saleInfoRepository.findByUserId(Objects.requireNonNull(saleUserId))
			.orElseThrow();

		PropertyCreateReq req = new PropertyCreateReq();
		req.setTitle("Gallery Prop");
		req.setPrice(new BigDecimal("100.00"));
		req.setDescription("d");
		req.setTypeId(type.getTypeId());
		req.setSaleUserId(info.getUserId());
		req.setAreaId(area.getAreaId());
		req.setIsPublic(true);
		req.setIsForRent(false);

		try {
			var createRes = mockMvc.perform(post("/api/properties")
					.header(AUTH_HEADER, BEARER_PREFIX + Objects.requireNonNull(accessToken))
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
			return mapper.readTree(createRes).path("data").path("propertyId").asLong();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private StoredFileMeta prepareFile(String path) {
		StoredFileMeta f = new StoredFileMeta();
		f.setOriginalName("test.jpg");
		f.setStoredName("stored_" + System.nanoTime() + ".jpg");
		f.setExt("jpg");
		f.setMimeType("image/jpeg");
		f.setSizeBytes(1024L);
		String uniqueSha = String.format("%064x", System.nanoTime() + System.currentTimeMillis() + path.hashCode());
		f.setSha256(uniqueSha);
		f.setAccessLevel("PUBLIC");
		f.setLocation("LOCAL");
		f.setRelativePath(Objects.requireNonNull(path));
		f.setCreatedAt(LocalDateTime.now());
		f.setUpdatedAt(LocalDateTime.now());
		return storedFileMetaRepository.saveAndFlush(f);
	}

	@Test
	@DisplayName("POST/GET/DELETE /api/properties/{id}/gallery works and returns ApiResponse")
	void gallery_crud_flow() throws Exception {
		String access = login();
		Long propertyId = prepareProperty(access);
		StoredFileMeta f = prepareFile("public/2025/01/gallery-1.jpg");

		String body = "{\"fileId\":" + Objects.requireNonNull(f.getFileId()) + "}";

		// add file into gallery
		mockMvc.perform(post("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Add file into gallery successfully"))
			.andExpect(jsonPath("$.data.propertyId").value(propertyId.intValue()))
			.andExpect(jsonPath("$.data.fileId").value(f.getFileId().intValue()));

		// list files by property
		mockMvc.perform(get("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property gallery files"))
			.andExpect(jsonPath("$.data[0].propertyId").value(propertyId.intValue()))
			.andExpect(jsonPath("$.data[0].fileId").value(f.getFileId().intValue()));

		// delete file from gallery
		mockMvc.perform(delete("/api/properties/{propertyId}/gallery/{fileId}", propertyId, f.getFileId())
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Delete file from gallery successfully"));
	}

	@Test
	@DisplayName("Gallery error cases: 404 and validation errors")
	void gallery_error_cases() throws Exception {
		String access = login();
		Long propertyId = 999999L;
		StoredFileMeta f = prepareFile("public/2025/01/gallery-error.jpg");

		// 404 when property not found on add
		String body = "{\"fileId\":" + Objects.requireNonNull(f.getFileId()) + "}";
		mockMvc.perform(post("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(body))
			.andExpect(status().isNotFound());

		// 400 validation when fileId missing
		Long validPropertyId = prepareProperty(access);
		mockMvc.perform(post("/api/properties/{propertyId}/gallery", validPropertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{}"))
			.andExpect(status().isBadRequest());

		// 404 when delete non-existing relation
		mockMvc.perform(delete("/api/properties/{propertyId}/gallery/{fileId}", validPropertyId, 999999L)
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Add multiple files to gallery and list them")
	void add_multiple_files() throws Exception {
		String access = login();
		Long propertyId = prepareProperty(access);
		StoredFileMeta f1 = prepareFile("public/2025/01/gallery-multi-1.jpg");
		StoredFileMeta f2 = prepareFile("public/2025/01/gallery-multi-2.jpg");
		StoredFileMeta f3 = prepareFile("public/2025/01/gallery-multi-3.jpg");

		// Add first file
		mockMvc.perform(post("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{\"fileId\":" + Objects.requireNonNull(f1.getFileId()) + "}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.fileId").value(f1.getFileId().intValue()));

		// Add second file
		mockMvc.perform(post("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{\"fileId\":" + Objects.requireNonNull(f2.getFileId()) + "}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.fileId").value(f2.getFileId().intValue()));

		// Add third file
		mockMvc.perform(post("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{\"fileId\":" + Objects.requireNonNull(f3.getFileId()) + "}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.fileId").value(f3.getFileId().intValue()));

		// List all files
		mockMvc.perform(get("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Property gallery files"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data.length()").value(3))
			.andExpect(jsonPath("$.data[0].propertyId").value(propertyId.intValue()))
			.andExpect(jsonPath("$.data[1].propertyId").value(propertyId.intValue()))
			.andExpect(jsonPath("$.data[2].propertyId").value(propertyId.intValue()));
	}

	@Test
	@DisplayName("Add duplicate file returns 409 Conflict")
	void add_duplicate_file_conflict() throws Exception {
		String access = login();
		Long propertyId = prepareProperty(access);
		StoredFileMeta f = prepareFile("public/2025/01/gallery-dup.jpg");

		String body = "{\"fileId\":" + Objects.requireNonNull(f.getFileId()) + "}";

		// Add first time - success
		mockMvc.perform(post("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(body))
			.andExpect(status().isOk());

		// Add duplicate - conflict
		mockMvc.perform(post("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(body))
			.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("Add file with non-existent fileId returns 404")
	void add_file_not_found() throws Exception {
		String access = login();
		Long propertyId = prepareProperty(access);

		mockMvc.perform(post("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{\"fileId\":999999}"))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Get gallery for non-existent property returns 404")
	void get_gallery_property_not_found() throws Exception {
		String access = login();

		mockMvc.perform(get("/api/properties/{propertyId}/gallery", 999999L)
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Delete file and verify it's removed from list")
	void delete_file_removes_from_list() throws Exception {
		String access = login();
		Long propertyId = prepareProperty(access);
		StoredFileMeta f1 = prepareFile("public/2025/01/gallery-del-1.jpg");
		StoredFileMeta f2 = prepareFile("public/2025/01/gallery-del-2.jpg");

		// Add two files
		mockMvc.perform(post("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{\"fileId\":" + Objects.requireNonNull(f1.getFileId()) + "}"))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{\"fileId\":" + Objects.requireNonNull(f2.getFileId()) + "}"))
			.andExpect(status().isOk());

		// Verify both files are in list
		mockMvc.perform(get("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(2));

		// Delete first file
		mockMvc.perform(delete("/api/properties/{propertyId}/gallery/{fileId}", propertyId, f1.getFileId())
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk());

		// Verify only second file remains
		mockMvc.perform(get("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].fileId").value(f2.getFileId().intValue()));
	}

	@Test
	@DisplayName("Response includes correct filePath from StoredFileMeta")
	void response_includes_file_path() throws Exception {
		String access = login();
		Long propertyId = prepareProperty(access);
		StoredFileMeta f = prepareFile("public/2025/01/gallery-path.jpg");

		mockMvc.perform(post("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content("{\"fileId\":" + Objects.requireNonNull(f.getFileId()) + "}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.filePath").value(f.getRelativePath()));

		mockMvc.perform(get("/api/properties/{propertyId}/gallery", propertyId)
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].filePath").value(f.getRelativePath()));
	}
}



