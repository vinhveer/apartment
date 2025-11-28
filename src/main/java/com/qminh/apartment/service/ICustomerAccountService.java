package com.qminh.apartment.service;

import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.dto.user.UserUpdateReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICustomerAccountService {

	Page<UserRes> searchCustomerAccounts(String q, Pageable pageable);

	UserRes editCustomerAccount(long id, UserUpdateReq req);

	void deleteCustomerAccount(long id);
}


