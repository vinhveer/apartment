package com.qminh.apartment.repository;

import com.qminh.apartment.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	@NonNull Optional<RefreshToken> findByToken(@NonNull String token);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query("update RefreshToken r set r.revoked = true where r.token = ?1")
	void revoke(@NonNull String token);
}


