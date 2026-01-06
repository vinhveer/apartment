package com.qminh.apartment.features.accounts.persistence;

import com.qminh.apartment.features.accounts.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	@EntityGraph(attributePaths = "role")
	@NonNull Optional<User> findByUsername(@NonNull String username);

	@EntityGraph(attributePaths = "role")
	@NonNull Optional<User> findByEmail(@NonNull String email);

	@EntityGraph(attributePaths = "role")
	@Query("""
		select u from User u
		where u.role.roleName = :role
		  and (:q is null
		       or u.username like :q
		       or u.email like :q
		       or coalesce(u.displayName, '') like :q)
		""")
	Page<User> searchByRole(@Param("role") String role, @Param("q") String q, Pageable pageable);

	@EntityGraph(attributePaths = "role")
	@Query("""
		select u from User u
		where u.role.roleName in :roles
		  and (:q is null
		       or u.username like :q
		       or u.email like :q
		       or coalesce(u.displayName, '') like :q)
		""")
	Page<User> searchByRoles(@Param("roles") Collection<String> roles, @Param("q") String q, Pageable pageable);
}

