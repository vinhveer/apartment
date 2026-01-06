package com.qvinh.apartment.features.accounts.application;

import com.qvinh.apartment.features.accounts.dto.account.AccountCreateReq;
import com.qvinh.apartment.features.accounts.dto.user.UserRes;
import com.qvinh.apartment.features.accounts.dto.user.UserUpdateReq;
import com.qvinh.apartment.features.accounts.dto.user.UserRoleUpdateReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAccountService {
	UserRes createEmployeeAccount(AccountCreateReq req);
	Page<UserRes> searchEmployeeAccounts(String q, Pageable pageable);
	UserRes editEmployeeAccount(long id, UserUpdateReq req);
	void deleteEmployeeAccount(long id);
	UserRes changeEmployeeRole(long id, UserRoleUpdateReq req);
}
