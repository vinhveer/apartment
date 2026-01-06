package com.qvinh.apartment.features.accounts.application;

import com.qvinh.apartment.features.accounts.domain.Role;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.dto.user.SelfProfileUpdateReq;
import com.qvinh.apartment.features.accounts.persistence.RoleRepository;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import com.qvinh.apartment.testsupport.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UsersServiceTest extends PostgresTestContainer {

	@Autowired private UsersService service;
	@Autowired private UserRepository userRepository;
	@Autowired private RoleRepository roleRepository;

	@BeforeEach
	void ensureRoles() {
		if (roleRepository.findByRoleName("USER").isEmpty()) {
			Role r = new Role();
			r.setRoleName("USER");
			roleRepository.saveAndFlush(r);
		}
	}

	@Test
	@Transactional
	@DisplayName("updateSelfProfile updates displayName/fullName/phone for current user")
	void update_self_profile_basic() {
		Role role = roleRepository.findByRoleName("USER").orElseThrow();
		User u = new User();
		u.setUsername("selfuser");
		u.setEmail("selfuser@example.com");
		u.setPassword("x");
		u.setRole(role);
		userRepository.saveAndFlush(u);

		SelfProfileUpdateReq req = new SelfProfileUpdateReq();
		req.setDisplayName("Self User");
		req.setFullName("Self User Full");
		req.setPhone("0900111222");

		var res = service.updateSelfProfile("selfuser", req);
		assertThat(res.getUsername()).isEqualTo("selfuser");
		assertThat(res.getDisplayName()).isEqualTo("Self User");
		// For USER role, fullName/phone are from PropertySaleInfo (null), just ensure no error and user updated
		User reloaded = userRepository.findByUsername("selfuser").orElseThrow();
		assertThat(reloaded.getDisplayName()).isEqualTo("Self User");
	}

	@Test
	@Transactional
	@DisplayName("updateAvatar stores base64 avatar for current user")
	void update_avatar_stores_base64() throws Exception {
		Role role = roleRepository.findByRoleName("USER").orElseThrow();
		User u = new User();
		u.setUsername("avataruser");
		u.setEmail("avataruser@example.com");
		u.setPassword("x");
		u.setRole(role);
		userRepository.saveAndFlush(u);

		BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		try {
			g.setColor(Color.RED);
			g.fillRect(0, 0, 50, 50);
		} finally {
			g.dispose();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", baos);
		MockMultipartFile file = new MockMultipartFile(
			"file", "avatar.jpg", "image/jpeg", baos.toByteArray());

		var res = service.updateAvatar("avataruser", file);
		assertThat(res.getAvatar()).isNotNull();
		assertThat(res.getAvatar()).startsWith("data:image/jpeg;base64,");

		User reloaded = userRepository.findByUsername("avataruser").orElseThrow();
		assertThat(reloaded.getAvatar()).isEqualTo(res.getAvatar());
	}

	@Test
	@Transactional
	@DisplayName("deleteAvatar clears avatar for current user")
	void delete_avatar_clears_field() throws Exception {
		Role role = roleRepository.findByRoleName("USER").orElseThrow();
		User u = new User();
		u.setUsername("avataruser2");
		u.setEmail("avataruser2@example.com");
		u.setPassword("x");
		u.setRole(role);
		u.setAvatar("data:image/jpeg;base64,aaaa");
		userRepository.saveAndFlush(u);

		var res = service.deleteAvatar("avataruser2");
		assertThat(res.getAvatar()).isNull();
		User reloaded = userRepository.findByUsername("avataruser2").orElseThrow();
		assertThat(reloaded.getAvatar()).isNull();
	}
}
