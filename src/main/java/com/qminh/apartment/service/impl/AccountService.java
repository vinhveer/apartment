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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		Role admin = roleRepository.findByRoleName("ADMIN").orElseThrow();
		User user = userMapper.toEntity(req);
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		user.setRole(admin);
		User saved = userRepository.save(user);
		return userMapper.toRes(saved);
	}
}


