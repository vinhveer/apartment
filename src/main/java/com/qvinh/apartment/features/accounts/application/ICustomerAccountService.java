package com.qvinh.apartment.features.accounts.application;

import com.qvinh.apartment.features.accounts.dto.user.UserRes;
import com.qvinh.apartment.features.accounts.dto.user.UserUpdateReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICustomerAccountService {

	Page<UserRes> searchCustomerAccounts(String q, Pageable pageable);

	UserRes editCustomerAccount(long id, UserUpdateReq req);

	void deleteCustomerAccount(long id);
}
