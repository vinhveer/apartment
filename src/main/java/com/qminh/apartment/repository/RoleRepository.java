package com.qminh.apartment.repository;

import com.qminh.apartment.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
	@NonNull Optional<Role> findByRoleName(@NonNull String roleName);
}


