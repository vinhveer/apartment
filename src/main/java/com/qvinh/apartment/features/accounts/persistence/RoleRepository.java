package com.qvinh.apartment.features.accounts.persistence;

import com.qvinh.apartment.features.accounts.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
	@NonNull Optional<Role> findByRoleName(@NonNull String roleName);
}

