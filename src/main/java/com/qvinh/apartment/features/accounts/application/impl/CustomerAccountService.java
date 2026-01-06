package com.qvinh.apartment.features.accounts.application.impl;

import com.qvinh.apartment.features.accounts.application.ICustomerAccountService;
import com.qvinh.apartment.features.accounts.domain.User;
import com.qvinh.apartment.features.accounts.dto.user.UserRes;
import com.qvinh.apartment.features.accounts.dto.user.UserUpdateReq;
import com.qvinh.apartment.features.accounts.constants.AccountsMessages;
import com.qvinh.apartment.shared.constants.RoleNames;
import com.qvinh.apartment.shared.error.ErrorCode;
import com.qvinh.apartment.shared.exception.ResourceNotFoundException;
import com.qvinh.apartment.features.accounts.mapper.UserMapper;
import com.qvinh.apartment.features.accounts.persistence.UserRepository;
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
		return userRepository.searchByRole(RoleNames.USER, pattern, pageable).map(userMapper::toRes);
	}

	@Override
	@Transactional
	public UserRes editCustomerAccount(long id, UserUpdateReq req) {
		User u = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, AccountsMessages.USER_NOT_FOUND));
		userMapper.updateEntityFromReq(req, u);
		User updated = userRepository.save(Objects.requireNonNull(u, "user must not be null"));
		return userMapper.toRes(updated);
	}

	@Override
	@Transactional
	public void deleteCustomerAccount(long id) {
		User u = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, AccountsMessages.USER_NOT_FOUND));
		userRepository.delete(Objects.requireNonNull(u, "user must not be null"));
	}
}
