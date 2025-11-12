package com.qminh.apartment.service.impl;

import com.qminh.apartment.dto.account.AdminCreateReq;
import com.qminh.apartment.dto.account.SaleCreateReq;
import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.entity.PropertySaleInfo;
import com.qminh.apartment.entity.Role;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.mapper.PropertySaleInfoMapper;
import com.qminh.apartment.mapper.UserMapper;
import com.qminh.apartment.repository.PropertySaleInfoRepository;
import com.qminh.apartment.repository.RoleRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.service.IAccountService;
import com.qminh.apartment.exception.ConflictException;
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
	public UserRes createSale(SaleCreateReq req) {
		// early duplicates check to return clear 409 CONFLICT instead of DB error
		userRepository.findByUsername(Objects.requireNonNull(req.getUsername())).ifPresent(u -> {
			throw new ConflictException("Username already exists: " + req.getUsername());
		});
		userRepository.findByEmail(Objects.requireNonNull(req.getEmail())).ifPresent(u -> {
			throw new ConflictException("Email already exists: " + req.getEmail());
		});
		Role sale = roleRepository.findByRoleName("SALE").orElseThrow();
		User user = userMapper.toEntity(req);
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		user.setRole(sale);
		User saved = userRepository.save(user);

		PropertySaleInfo info = saleInfoMapper.toEntity(req);
		info.setUser(saved);
		saleInfoRepository.save(info);

		return userMapper.toRes(saved);
	}

	@Transactional
	public UserRes createAdmin(AdminCreateReq req) {
		// early duplicates check to return clear 409 CONFLICT instead of DB error
		userRepository.findByUsername(Objects.requireNonNull(req.getUsername())).ifPresent(u -> {
			throw new ConflictException("Username already exists: " + req.getUsername());
		});
		userRepository.findByEmail(Objects.requireNonNull(req.getEmail())).ifPresent(u -> {
			throw new ConflictException("Email already exists: " + req.getEmail());
		});
		Role admin = roleRepository.findByRoleName("ADMIN").orElseThrow();
		User user = userMapper.toEntity(req);
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		user.setRole(admin);
		User saved = userRepository.save(user);
		return userMapper.toRes(saved);
	}
}


