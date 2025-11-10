package com.qminh.apartment.repository;

import com.qminh.apartment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.lang.NonNull;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	@EntityGraph(attributePaths = "role")
	@NonNull Optional<User> findByUsername(@NonNull String username);
	@EntityGraph(attributePaths = "role")
	@NonNull Optional<User> findByEmail(@NonNull String email);
}


