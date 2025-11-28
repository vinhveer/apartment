package com.qminh.apartment.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UsersService implements IUsersService {

	private final UserRepository userRepository;
	private final PropertySaleInfoRepository saleInfoRepository;
	private final RoleRepository roleRepository;
	private final UserMapper userMapper;
	private static final String NOT_FOUND = "User not found: ";

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
}


