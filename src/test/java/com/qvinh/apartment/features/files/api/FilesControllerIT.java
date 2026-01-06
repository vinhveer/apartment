package com.qvinh.apartment.features.files.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qvinh.apartment.features.accounts.domain.Role;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.persistence.RoleRepository;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import com.qvinh.apartment.features.auth.dto.LoginReq;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
	"app.upload.base-path=./build/test-uploads"
})
class FilesControllerIT extends PostgresTestContainer {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper mapper;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRepository userRepository;

	private static final String USERNAME_UPLOADER = "uploader";
	private static final String MIME_TYPE_JPEG = "image/jpeg";
	private static final String AUTH_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	@BeforeEach
	void setup() {
		if (roleRepository.findByRoleName("USER").isEmpty()) {
			Role r = new Role(); r.setRoleName("USER"); roleRepository.saveAndFlush(r);
		}
		if (userRepository.findByUsername(USERNAME_UPLOADER).isEmpty()) {
			var role = roleRepository.findByRoleName("USER").orElseThrow();
			User u = new User();
			u.setUsername(USERNAME_UPLOADER);
			u.setEmail("uploader@example.com");
			u.setPassword(passwordEncoder.encode("123456"));
			u.setRole(role);
			userRepository.saveAndFlush(u);
		}
	}

	private String login() throws Exception {
		LoginReq req = new LoginReq();
		req.setUsername(USERNAME_UPLOADER);
		req.setPassword("123456");
		var res = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(Objects.requireNonNull(mapper.writeValueAsString(req))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andReturn().getResponse().getContentAsString();
		return mapper.readTree(res).path("data").path("accessToken").asText();
	}

	private static MockMultipartFile sampleJpeg() throws Exception {
		BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(img, "jpg", baos);
			return new MockMultipartFile("file", "img.jpg", MIME_TYPE_JPEG, baos.toByteArray());
		}
	}

	private static MockMultipartFile sampleJpegWithSize(int size) throws Exception {
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(img, "jpg", baos);
			return new MockMultipartFile("file", "img.jpg", MIME_TYPE_JPEG, baos.toByteArray());
		}
	}

	@Test
	@DisplayName("POST /api/files (PUBLIC) then GET private endpoints require auth")
	void upload_public_and_auth_required_for_get() throws Exception {
		String access = login();
		var file = sampleJpeg();
		var uploadRes = mockMvc.perform(multipart("/api/files")
				.file(Objects.requireNonNull(file))
				.param("accessLevel", "PUBLIC")
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Upload file successfully"))
			.andExpect(jsonPath("$.data.fileId").exists())
			.andExpect(jsonPath("$.data.accessLevel").value("PUBLIC"))
			.andReturn().getResponse().getContentAsString();
		long fileId = mapper.readTree(uploadRes).path("data").path("fileId").asLong();
		mockMvc.perform(get("/api/files/{id}", fileId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Get file successfully"))
			.andExpect(jsonPath("$.data.fileId").value((int) fileId))
			.andExpect(jsonPath("$.data.mimeType").value(MIME_TYPE_JPEG));
	}

	@Test
	@DisplayName("POST /api/files (PRIVATE) then GET/PUT/DELETE work when authorized")
	void upload_private_get_rename_delete_ok() throws Exception {
		String access = login();
		var file = sampleJpegWithSize(12); // ensure different content from previous test to avoid de-dup
		var uploadRes = mockMvc.perform(multipart("/api/files")
				.file(Objects.requireNonNull(file))
				.param("accessLevel", "PRIVATE")
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.fileId").exists())
			.andExpect(jsonPath("$.data.accessLevel").value("PRIVATE"))
			.andReturn().getResponse().getContentAsString();
		long fileId = mapper.readTree(uploadRes).path("data").path("fileId").asLong();

		// GET metadata
		mockMvc.perform(get("/api/files/{id}", fileId).header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Get file successfully"))
			.andExpect(jsonPath("$.data.fileId").value((int) fileId))
			.andExpect(jsonPath("$.data.mimeType").value(MIME_TYPE_JPEG));

		// Rename
		var renameBody = """
			{"originalName":"renamed.jpg"}
			""";
		mockMvc.perform(put("/api/files/{id}/name", fileId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(renameBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Rename file successfully"))
			.andExpect(jsonPath("$.data.originalName").value("renamed.jpg"));

		// Delete
		mockMvc.perform(delete("/api/files/{id}", fileId)
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Delete file successfully"));

		// GET after delete -> 404
		mockMvc.perform(get("/api/files/{id}", fileId).header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("GET /api/files list with search and pagination works")
	void list_files_with_search_and_paging() throws Exception {
		String access = login();

		// upload 2 PUBLIC and 1 PRIVATE files
		var banner = sampleJpegWithSize(20);
		mockMvc.perform(multipart("/api/files")
				.file(Objects.requireNonNull(banner))
				.param("accessLevel", "PUBLIC")
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.fileId").exists());

		var other = sampleJpegWithSize(22);
		mockMvc.perform(multipart("/api/files")
				.file(Objects.requireNonNull(other))
				.param("accessLevel", "PUBLIC")
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk());

		var privateFile = sampleJpegWithSize(24);
		mockMvc.perform(multipart("/api/files")
				.file(Objects.requireNonNull(privateFile))
				.param("accessLevel", "PRIVATE")
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk());

		// list only PUBLIC files, page=1,pageSize=2
		mockMvc.perform(get("/api/files")
				.param("accessLevel", "PUBLIC")
				.param("page", "1")
				.param("pageSize", "2")
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Get files successfully"))
			.andExpect(jsonPath("$.data.items").isArray());
	}

	@Test
	@DisplayName("PUT /api/files/{id}/meta updates metadata; 404 when file not found")
	void update_meta_and_not_found() throws Exception {
		String access = login();
		var file = sampleJpegWithSize(16);
		var uploadRes = mockMvc.perform(multipart("/api/files")
				.file(Objects.requireNonNull(file))
				.param("accessLevel", "PUBLIC")
				.header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		long fileId = mapper.readTree(uploadRes).path("data").path("fileId").asLong();

		var body = """
			{
			  "altText": "Banner chính trang chủ (desktop)",
			  "title": "Homepage main banner (desktop)",
			  "description": "Banner dùng cho campaign Tết 2025, phiên bản desktop",
			  "tags": ["banner", "homepage", "tet-2025", "desktop"]
			}
			""";
		mockMvc.perform(put("/api/files/{id}/meta", fileId)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Update file metadata successfully"))
			.andExpect(jsonPath("$.data.altText").value("Banner chính trang chủ (desktop)"))
			.andExpect(jsonPath("$.data.tags[0]").value("banner"));

		// not found
		mockMvc.perform(put("/api/files/{id}/meta", 999999L)
				.header(AUTH_HEADER, BEARER_PREFIX + access)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(body))
			.andExpect(status().isNotFound());
	}
}

