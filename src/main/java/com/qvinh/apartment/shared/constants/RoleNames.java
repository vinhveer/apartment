package com.qvinh.apartment.shared.constants;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class RoleNames {

	public static final String ROLE_PREFIX = "ROLE_";

	public static final String ADMIN = "ADMIN";
	public static final String SALE = "SALE";
	public static final String USER = "USER";

	public static final List<String> EMPLOYEE_ROLES = List.of(ADMIN, SALE);

	public static final String EMPLOYEE_ROLE_REGEX = "^(ADMIN|SALE)$";

	private RoleNames() {}

	public static String normalize(String roleName) {
		return Objects.requireNonNull(roleName, "roleName must not be null").toUpperCase(Locale.ROOT);
	}

	public static boolean isEmployeeRole(String roleName) {
		return ADMIN.equals(roleName) || SALE.equals(roleName);
	}
}

