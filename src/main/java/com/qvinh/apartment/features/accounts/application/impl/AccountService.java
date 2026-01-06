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
import com.qvinh.apartment.features.accounts.constants.AccountsMessages;
import com.qvinh.apartment.shared.constants.DefaultValues;
import com.qvinh.apartment.shared.constants.RoleNames;
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

import java.util.Locale;
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
			throw new ConflictException(ErrorCode.USERNAME_ALREADY_EXISTS, AccountsMessages.USERNAME_ALREADY_EXISTS);
		});

		userRepository.findByEmail(Objects.requireNonNull(req.getEmail())).ifPresent(u -> {
			throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS, AccountsMessages.EMAIL_ALREADY_EXISTS);
		});

		String roleName = normalizeEmployeeRoleName(req.getRoleName());
		
		if (!isEmployeeRole(roleName)) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.UNPROCESSABLE_ENTITY, AccountsMessages.INVALID_ROLE_NAME);
		}

		Role role = roleRepository.findByRoleName(roleName)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND, AccountsMessages.ROLE_NOT_FOUND));
		
		User user = userMapper.toEntity(req);
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		user.setRole(role);

		if (RoleNames.ADMIN.equals(roleName)) {
			if (req.getFullName() == null || req.getFullName().isBlank()) {
				String fallbackName = req.getDisplayName() != null && !req.getDisplayName().isBlank()
					? req.getDisplayName()
					: req.getUsername();
				req.setFullName(fallbackName);
			}
			if (req.getPhone() == null || req.getPhone().isBlank()) {
				req.setPhone(DefaultValues.NOT_AVAILABLE);
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
		return userRepository.searchByRoles(RoleNames.EMPLOYEE_ROLES, pattern, pageable).map(userMapper::toRes);
	}

	@Transactional
	public UserRes editEmployeeAccount(long id, UserUpdateReq req) {
		User u = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, AccountsMessages.USER_NOT_FOUND));

		userMapper.updateEntityFromReq(req, u);
		User updated = userRepository.save(Objects.requireNonNull(u, "user must not be null"));
		maybeUpdateSaleInfo(updated, req);

		return userMapper.toRes(updated);
	}

	@Transactional
	public void deleteEmployeeAccount(long id) {
		// delete sale info first if exists (FK constraint)
		saleInfoRepository.findByUserId(id).ifPresent(info -> saleInfoRepository.deleteByUserId(id));
		User u = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, AccountsMessages.USER_NOT_FOUND));
		userRepository.delete(Objects.requireNonNull(u, "user must not be null"));
	}

	@Transactional
	public UserRes changeEmployeeRole(long id, UserRoleUpdateReq req) {
		User u = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, AccountsMessages.USER_NOT_FOUND));

		String targetRole = normalizeEmployeeRoleName(req.getRoleName());

		if (!isEmployeeRole(targetRole)) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.UNPROCESSABLE_ENTITY, AccountsMessages.INVALID_ROLE_NAME);
		}

		Role role = roleRepository.findByRoleName(targetRole)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND, AccountsMessages.ROLE_NOT_FOUND));

		String currentRole = Objects.requireNonNull(u.getRole()).getRoleName();
		if (currentRole.equals(targetRole)) {
			return userMapper.toRes(u);
		}

		u.setRole(role);
		User saved = userRepository.save(u);

		if (RoleNames.SALE.equals(targetRole)) {
			String fullName = resolveFullName(req, u);
			String phone = resolvePhone(req);
			upsertSaleInfo(saved, fullName, phone);
		}

		return userMapper.toRes(saved);
	}

	private static String normalizeEmployeeRoleName(String roleName) {
		return Objects.requireNonNull(roleName, "roleName must not be null").toUpperCase(Locale.ROOT);
	}

	private static boolean isEmployeeRole(String roleName) {
		return RoleNames.isEmployeeRole(roleName);
	}

	private static String resolveFullName(UserRoleUpdateReq req, User u) {
		String fromReq = req.getFullName();
		if (fromReq != null && !fromReq.isBlank()) return fromReq;
		String displayName = u.getDisplayName();
		if (displayName != null && !displayName.isBlank()) return displayName;
		return u.getUsername();
	}

	private static String resolvePhone(UserRoleUpdateReq req) {
		String fromReq = req.getPhone();
		return (fromReq != null && !fromReq.isBlank()) ? fromReq : DefaultValues.NOT_AVAILABLE;
	}

	private static boolean hasSaleInfoUpdate(UserUpdateReq req) {
		return req.getFullName() != null || req.getPhone() != null;
	}

	private static boolean canCreateSaleInfo(UserUpdateReq req) {
		return req.getFullName() != null && !req.getFullName().isBlank()
			&& req.getPhone() != null && !req.getPhone().isBlank();
	}

	private void maybeUpdateSaleInfo(User updated, UserUpdateReq req) {
		String roleName = Objects.requireNonNull(updated.getRole()).getRoleName();
		if (!isEmployeeRole(roleName) || !hasSaleInfoUpdate(req)) return;

		long userId = Objects.requireNonNull(updated.getId(), "userId must not be null");

		saleInfoRepository.findByUserId(userId).ifPresentOrElse(existing -> {
			if (req.getFullName() != null && !req.getFullName().isBlank()) {
				existing.setFullName(req.getFullName());
			}
			if (req.getPhone() != null && !req.getPhone().isBlank()) {
				existing.setPhone(req.getPhone());
			}
			saleInfoRepository.save(existing);
		}, () -> {
			if (!canCreateSaleInfo(req)) return;
			PropertySaleInfo info = new PropertySaleInfo();
			info.setUser(updated);
			info.setFullName(req.getFullName());
			info.setPhone(req.getPhone());
			saleInfoRepository.save(info);
		});
	}

	private void upsertSaleInfo(User user, String fullName, String phone) {
		long userId = Objects.requireNonNull(user.getId(), "userId must not be null");
		
		saleInfoRepository.findByUserId(userId).ifPresentOrElse(existing -> {
			existing.setFullName(fullName);
			existing.setPhone(phone);
			saleInfoRepository.save(existing);
		}, () -> {
			PropertySaleInfo info = new PropertySaleInfo();
			info.setUser(user);
			info.setFullName(fullName);
			info.setPhone(phone);
			saleInfoRepository.save(info);
		});
	}
}
