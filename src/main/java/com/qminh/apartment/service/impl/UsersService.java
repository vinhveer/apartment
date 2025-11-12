package com.qminh.apartment.service.impl;

import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.dto.user.UserUpdateReq;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.exception.ResourceNotFoundException;
import com.qminh.apartment.mapper.UserMapper;
import com.qminh.apartment.repository.PropertySaleInfoRepository;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.service.IUsersService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UsersService implements IUsersService {

	private final UserRepository userRepository;
	private final PropertySaleInfoRepository saleInfoRepository;
	private final UserMapper userMapper;
	private static final String NOT_FOUND = "User not found: ";

	public UsersService(UserRepository userRepository, PropertySaleInfoRepository saleInfoRepository, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.saleInfoRepository = saleInfoRepository;
		this.userMapper = userMapper;
	}

	@Transactional(readOnly = true)
	public UserRes get(long id) {
		User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		return userMapper.toRes(u);
	}

	@Transactional(readOnly = true)
	public Page<UserRes> list(Pageable pageable) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		return userRepository.findAll(pageable).map(userMapper::toRes);
	}

	@Transactional
	public UserRes update(long id, UserUpdateReq req) {
		User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		userMapper.updateEntityFromReq(req, u);
		User updated = userRepository.save(Objects.requireNonNull(u, "user must not be null"));
		return userMapper.toRes(updated);
	}

	@Transactional
	public void delete(long id) {
		// delete sale info first if exists (FK constraint)
		saleInfoRepository.findByUserId(id).ifPresent(info -> saleInfoRepository.deleteByUserId(id));
		User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + id));
		userRepository.delete(Objects.requireNonNull(u, "user must not be null"));
	}
}


