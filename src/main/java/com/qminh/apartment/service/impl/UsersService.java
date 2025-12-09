package com.qminh.apartment.service.impl;

import com.qminh.apartment.dto.user.SelfProfileUpdateReq;
import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.dto.user.UserRoleUpdateReq;
import com.qminh.apartment.entity.PropertySaleInfo;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.entity.Role;
import com.qminh.apartment.exception.ResourceNotFoundException;
import com.qminh.apartment.mapper.UserMapper;
import com.qminh.apartment.repository.PropertySaleInfoRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.repository.RoleRepository;
import com.qminh.apartment.service.IUsersService;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
public class UsersService implements IUsersService {

	private final UserRepository userRepository;
	private final PropertySaleInfoRepository saleInfoRepository;
	private final RoleRepository roleRepository;
	private final UserMapper userMapper;
	private static final String NOT_FOUND = "User not found: ";
	private static final int AVATAR_MAX_SIZE = 200;
	private static final List<String> ALLOWED_MIME = List.of("image/jpeg", "image/png", "image/webp");

	public UsersService(UserRepository userRepository, PropertySaleInfoRepository saleInfoRepository, RoleRepository roleRepository, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.saleInfoRepository = saleInfoRepository;
		this.roleRepository = roleRepository;
		this.userMapper = userMapper;
	}

	@Transactional
	public UserRes updateRole(long id, UserRoleUpdateReq req) {
		User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		String targetRole = Objects.requireNonNull(req.getRoleName()).toUpperCase();
		if (!targetRole.equals("ADMIN") && !targetRole.equals("SALE")) {
			throw new IllegalArgumentException("roleName must be ADMIN or SALE");
		}
		Role role = roleRepository.findByRoleName(targetRole).orElseThrow();
		String current = Objects.requireNonNull(u.getRole()).getRoleName();
		if (current.equals(targetRole)) {
			return userMapper.toRes(u);
		}
		if (targetRole.equals("SALE")) {
			u.setRole(role);
			User saved = userRepository.save(u);
			// Backfill fullName/phone if not provided (similar to createEmployeeAccount logic)
			String fullName = (req.getFullName() != null && !req.getFullName().isBlank())
				? req.getFullName()
				: (u.getDisplayName() != null && !u.getDisplayName().isBlank() ? u.getDisplayName() : u.getUsername());
			String phone = (req.getPhone() != null && !req.getPhone().isBlank()) ? req.getPhone() : "N/A";
			// Check if sale info already exists (e.g. user was ADMIN with sale info)
			saleInfoRepository.findByUserId(id).ifPresentOrElse(existing -> {
				existing.setFullName(fullName);
				existing.setPhone(phone);
				saleInfoRepository.save(existing);
			}, () -> {
				PropertySaleInfo info = new PropertySaleInfo();
				info.setUser(saved);
				info.setFullName(fullName);
				info.setPhone(phone);
				saleInfoRepository.save(info);
			});
			return userMapper.toRes(saved);
		} else {
			// Switch to ADMIN: remove sale info if exists
			saleInfoRepository.findByUserId(id).ifPresent(i -> saleInfoRepository.deleteByUserId(id));
			u.setRole(role);
			User saved = userRepository.save(u);
			return userMapper.toRes(saved);
		}
	}

	@Transactional
	public UserRes updateAvatar(String username, MultipartFile file) {
		Objects.requireNonNull(file, "file must not be null");
		User user = userRepository.findByUsername(Objects.requireNonNull(username))
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

		// Validate file size (max 5MB)
		if (file.getSize() > 5 * 1024 * 1024) {
			throw new IllegalArgumentException("File size too large. Maximum size is 5MB.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_MIME.contains(contentType.toLowerCase())) {
			throw new IllegalArgumentException("Only JPEG, PNG, WEBP images are allowed. Detected type: " + contentType);
		}

		try {
			BufferedImage original = ImageIO.read(file.getInputStream());
			if (original == null) {
				throw new IllegalArgumentException("Invalid image file: cannot read image data. Please ensure the file is a valid image.");
			}

			BufferedImage resized = Thumbnails.of(original)
				.size(AVATAR_MAX_SIZE, AVATAR_MAX_SIZE)
				.keepAspectRatio(true)
				.asBufferedImage();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Thumbnails.of(resized)
				.scale(1.0)
				.outputFormat("jpg")
				.outputQuality(0.8)
				.toOutputStream(baos);

			String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
			String dataUri = "data:image/jpeg;base64," + base64;

			user.setAvatar(dataUri);
			User saved = userRepository.save(user);
			return userMapper.toRes(saved);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to process image: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error processing avatar: " + e.getMessage(), e);
		}
	}

	@Transactional
	public UserRes deleteAvatar(String username) {
		User user = userRepository.findByUsername(Objects.requireNonNull(username))
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
		user.setAvatar(null);
		User saved = userRepository.save(user);
		return userMapper.toRes(saved);
	}

	@Transactional
	public UserRes updateSelfProfile(String username, SelfProfileUpdateReq req) {
		User user = userRepository.findByUsername(Objects.requireNonNull(username))
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
		userMapper.updateEntityFromSelf(req, user);
		User updated = userRepository.save(Objects.requireNonNull(user, "user must not be null"));
		String roleName = Objects.requireNonNull(updated.getRole()).getRoleName();
		if (("ADMIN".equals(roleName) || "SALE".equals(roleName)) &&
			(req.getFullName() != null || req.getPhone() != null)) {
			Long userId = Objects.requireNonNull(updated.getId(), "user id must not be null");
			saleInfoRepository.findByUserId(userId).ifPresent(existing -> {
				PropertySaleInfo info = Objects.requireNonNull(existing, "existing sale info must not be null");
				if (req.getFullName() != null && !req.getFullName().isBlank()) {
					info.setFullName(req.getFullName());
				}
				if (req.getPhone() != null && !req.getPhone().isBlank()) {
					info.setPhone(req.getPhone());
				}
				saleInfoRepository.save(info);
			});
		}
		return userMapper.toRes(updated);
	}
}


