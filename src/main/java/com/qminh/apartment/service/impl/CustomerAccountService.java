package com.qminh.apartment.service.impl;

import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.dto.user.UserUpdateReq;
import com.qminh.apartment.entity.User;
import com.qminh.apartment.exception.ResourceNotFoundException;
import com.qminh.apartment.mapper.UserMapper;
import com.qminh.apartment.repository.UserRepository;
import com.qminh.apartment.service.ICustomerAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class CustomerAccountService implements ICustomerAccountService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	public CustomerAccountService(UserRepository userRepository, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserRes> searchCustomerAccounts(String q, Pageable pageable) {
		Objects.requireNonNull(pageable, "pageable must not be null");
		String pattern = (q == null || q.isBlank()) ? null : "%" + q.trim() + "%";
		return userRepository.searchByRole("USER", pattern, pageable).map(userMapper::toRes);
	}

	@Override
	@Transactional
	public UserRes editCustomerAccount(long id, UserUpdateReq req) {
		User u = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		userMapper.updateEntityFromReq(req, u);
		User updated = userRepository.save(Objects.requireNonNull(u, "user must not be null"));
		return userMapper.toRes(updated);
	}

	@Override
	@Transactional
	public void deleteCustomerAccount(long id) {
		User u = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
		userRepository.delete(Objects.requireNonNull(u, "user must not be null"));
	}
}


