package com.qvinh.apartment.features.accounts.application.impl;

import com.qvinh.apartment.features.accounts.application.IAccountService;
import com.qvinh.apartment.features.accounts.domain.PropertySaleInfo;
import com.qvinh.apartment.features.accounts.domain.Role;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.dto.account.AccountCreateReq;
import com.qvinh.apartment.features.accounts.dto.user.UserRes;
import com.qvinh.apartment.features.accounts.dto.user.UserUpdateReq;
import com.qvinh.apartment.features.accounts.dto.user.UserRoleUpdateReq;
import com.qvinh.apartment.features.accounts.mapper.PropertySaleInfoMapper;
import com.qvinh.apartment.features.accounts.mapper.UserMapper;
import com.qvinh.apartment.features.accounts.persistence.PropertySaleInfoRepository;
import com.qvinh.apartment.features.accounts.persistence.RoleRepository;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
import com.qvinh.apartment.shared.error.ErrorCode;
import com.qvinh.apartment.shared.exception.ConflictException;
import com.qvinh.apartment.shared.exception.AppException;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class AccountService implements IAccountService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PropertySaleInfoRepository saleInfoRepository;
	private final UserMapper userMapper;
	private final PropertySaleInfoMapper saleInfoMapper;
	private final PasswordEncoder passwordEncoder;

	public AccountService(UserRepository userRepository, RoleRepository roleRepository,
	                      PropertySaleInfoRepository saleInfoRepository,
	                      UserMapper userMapper, PropertySaleInfoMapper saleInfoMapper,
	                      PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.saleInfoRepository = saleInfoRepository;
		this.userMapper = userMapper;
		this.saleInfoMapper = saleInfoMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UserRes createEmployeeAccount(AccountCreateReq req) {
		userRepository.findByUsername(Objects.requireNonNull(req.getUsername())).ifPresent(u -> {
			throw new ConflictException(ErrorCode.USERNAME_ALREADY_EXISTS, "Username already exists");
		});
		userRepository.findByEmail(Objects.requireNonNull(req.getEmail())).ifPresent(u -> {
			throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already exists");
		});
		String roleName = Objects.requireNonNull(req.getRoleName()).toUpperCase();
		if (!roleName.equals("ADMIN") && !roleName.equals("SALE")) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.UNPROCESSABLE_ENTITY, "Invalid roleName");
		}
		Role role = roleRepository.findByRoleName(roleName)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND, "Role not found"));
		User user = userMapper.toEntity(req);
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		user.setRole(role);

		if ("ADMIN".equals(roleName)) {
			if (req.getFullName() == null || req.getFullName().isBlank()) {
				String fallbackName = req.getDisplayName() != null && !req.getDisplayName().isBlank()
					? req.getDisplayName()
					: req.getUsername();
				req.setFullName(fallbackName);
			}
			if (req.getPhone() == null || req.getPhone().isBlank()) {
				req.setPhone("N/A");
			}
		}

		// fullName/phone required for all employee accounts (ADMIN and SALE)
		if (req.getFullName() == null || req.getFullName().isBlank() || req.getPhone() == null || req.getPhone().isBlank()) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.UNPROCESSABLE_ENTITY, "fullName and phone are required");
		}

		User saved = userRepository.save(user);
		PropertySaleInfo info = saleInfoMapper.toEntity(req);
		info.setUser(saved);
		saleInfoRepository.save(info);

		return userMapper.toRes(saved);
	}

	@Transactional(readOnly = true)
	public Page<UserRes> searchEmployeeAccounts(String q, Pageable pageable) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		String pattern = (q == null || q.isBlank()) ? null : "%" + q.trim() + "%";
		java.util.List<String> roles = java.util.List.of("ADMIN", "SALE");
		return userRepository.searchByRoles(roles, pattern, pageable).map(userMapper::toRes);
	}

	@Transactional
	public UserRes editEmployeeAccount(long id, UserUpdateReq req) {
		User u = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));
		userMapper.updateEntityFromReq(req, u);
		User updated = userRepository.save(Objects.requireNonNull(u, "user must not be null"));
		// Update PropertySaleInfo if fullName/phone provided and user is employee (ADMIN/SALE)
		String roleName = Objects.requireNonNull(updated.getRole()).getRoleName();
		if (("ADMIN".equals(roleName) || "SALE".equals(roleName)) &&
			(req.getFullName() != null || req.getPhone() != null)) {
			saleInfoRepository.findByUserId(id).ifPresentOrElse(existing -> {
				PropertySaleInfo info = Objects.requireNonNull(existing, "existing sale info must not be null");
				if (req.getFullName() != null && !req.getFullName().isBlank()) {
					info.setFullName(req.getFullName());
				}
				if (req.getPhone() != null && !req.getPhone().isBlank()) {
					info.setPhone(req.getPhone());
				}
				saleInfoRepository.save(info);
			}, () -> {
				// Create sale info if not exists (should not happen for employee accounts, but handle gracefully)
				if (req.getFullName() != null && !req.getFullName().isBlank() &&
					req.getPhone() != null && !req.getPhone().isBlank()) {
					PropertySaleInfo info = new PropertySaleInfo();
					info.setUser(Objects.requireNonNull(updated, "updated user must not be null"));
					info.setFullName(req.getFullName());
					info.setPhone(req.getPhone());
					saleInfoRepository.save(info);
				}
			});
		}
		return userMapper.toRes(updated);
	}

	@Transactional
	public void deleteEmployeeAccount(long id) {
		// delete sale info first if exists (FK constraint)
		saleInfoRepository.findByUserId(id).ifPresent(info -> saleInfoRepository.deleteByUserId(id));
		User u = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));
		userRepository.delete(Objects.requireNonNull(u, "user must not be null"));
	}

	@Transactional
	public UserRes changeEmployeeRole(long id, UserRoleUpdateReq req) {
		User u = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));
		String targetRole = Objects.requireNonNull(req.getRoleName()).toUpperCase();
		if (!targetRole.equals("ADMIN") && !targetRole.equals("SALE")) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.UNPROCESSABLE_ENTITY, "Invalid roleName");
		}
		Role role = roleRepository.findByRoleName(targetRole)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND, "Role not found"));
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
			// If sale info already exists (e.g. user was created as ADMIN with sale info), update it; otherwise create new
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
			// Switch to ADMIN: keep sale info if exists, only change role
			u.setRole(role);
			User saved = userRepository.save(u);
			return userMapper.toRes(saved);
		}
	}
}
