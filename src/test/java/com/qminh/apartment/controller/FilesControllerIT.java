package com.qminh.apartment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qminh.apartment.dto.auth.LoginReq;
import com.qminh.apartment.entity.Role;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.repository.RoleRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.testsupport.PostgresTestContainer;
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
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("POST /api/files (PRIVATE) then GET returns content when authorized")
	void upload_private_and_get_ok() throws Exception {
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
		mockMvc.perform(get("/api/files/{id}", fileId).header(AUTH_HEADER, BEARER_PREFIX + access))
			.andExpect(status().isOk())
			.andExpect(header().string("Content-Type", MIME_TYPE_JPEG));
	}
}


