package com.qvinh.apartment.features.accounts.application.impl;

import com.qvinh.apartment.features.accounts.application.IUsersService;
import com.qvinh.apartment.features.accounts.domain.PropertySaleInfo;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.dto.user.SelfProfileUpdateReq;
import com.qvinh.apartment.features.accounts.dto.user.UserRes;
import com.qvinh.apartment.shared.error.ErrorCode;
import com.qvinh.apartment.shared.exception.AppException;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.features.accounts.mapper.UserMapper;
import com.qvinh.apartment.features.accounts.persistence.PropertySaleInfoRepository;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
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
import org.springframework.http.HttpStatus;

@Service
public class UsersService implements IUsersService {

	private final UserRepository userRepository;
	private final PropertySaleInfoRepository saleInfoRepository;
	private final UserMapper userMapper;

	private static final int AVATAR_MAX_SIZE = 200;
	private static final List<String> ALLOWED_MIME = List.of("image/jpeg", "image/png", "image/webp");

	public UsersService(UserRepository userRepository, PropertySaleInfoRepository saleInfoRepository, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.saleInfoRepository = saleInfoRepository;
		this.userMapper = userMapper;
	}

	@Transactional
	public UserRes getMe(String username) {
		User user = userRepository.findByUsername(Objects.requireNonNull(username))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));
		return userMapper.toRes(user);
	}

	@Transactional
	public UserRes updateAvatar(String username, MultipartFile file) {
		Objects.requireNonNull(file, "file must not be null");
		User user = userRepository.findByUsername(Objects.requireNonNull(username))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

		// Validate file size (max 5MB)
		if (file.getSize() > 5 * 1024 * 1024) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.UNPROCESSABLE_ENTITY, "File size too large");
		}

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_MIME.contains(contentType.toLowerCase())) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.UNPROCESSABLE_ENTITY, "Unsupported image type");
		}

		try {
			BufferedImage original = ImageIO.read(file.getInputStream());
			if (original == null) {
				throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.UNPROCESSABLE_ENTITY, "Invalid image file");
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
			throw new AppException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image");
		} catch (Exception e) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image");
		}
	}

	@Transactional
	public UserRes deleteAvatar(String username) {
		User user = userRepository.findByUsername(Objects.requireNonNull(username))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));
		user.setAvatar(null);
		User saved = userRepository.save(user);
		return userMapper.toRes(saved);
	}

	@Transactional
	public UserRes updateSelfProfile(String username, SelfProfileUpdateReq req) {
		User user = userRepository.findByUsername(Objects.requireNonNull(username))
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));
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
