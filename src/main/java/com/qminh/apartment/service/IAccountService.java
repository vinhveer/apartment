package com.qminh.apartment.service;

import com.qminh.apartment.dto.account.AccountCreateReq;
import com.qminh.apartment.dto.user.UserRes;
import com.qminh.apartment.dto.user.UserUpdateReq;
import com.qminh.apartment.dto.user.UserRoleUpdateReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAccountService {
	UserRes createEmployeeAccount(AccountCreateReq req);
	Page<UserRes> searchEmployeeAccounts(String q, Pageable pageable);
	UserRes editEmployeeAccount(long id, UserUpdateReq req);
	void deleteEmployeeAccount(long id);
	UserRes changeEmployeeRole(long id, UserRoleUpdateReq req);
}


