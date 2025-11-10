package com.qminh.apartment.service;

import com.qminh.apartment.dto.account.AdminCreateReq;
import com.qminh.apartment.dto.account.SaleCreateReq;
import com.qminh.apartment.dto.user.UserRes;

public interface IAccountService {
	UserRes createSale(SaleCreateReq req);
	UserRes createAdmin(AdminCreateReq req);
}


