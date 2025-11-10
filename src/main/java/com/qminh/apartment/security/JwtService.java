package com.qminh.apartment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

	private final SecretKey key;

	public JwtService(@Value("${app.jwt.secret}") String secret) {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateAccess(String username, List<String> authorities, long ttlMs) {
		Instant now = Instant.now();
		return Jwts.builder()
			.subject(username)
			.id(UUID.randomUUID().toString())
			.claim("authorities", authorities)
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plusMillis(ttlMs)))
			.signWith(key)
			.compact();
	}

	public String generateRefresh(String username, long ttlMs) {
		Instant now = Instant.now();
		return Jwts.builder()
			.subject(username)
			.id(UUID.randomUUID().toString())
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plusMillis(ttlMs)))
			.signWith(key)
			.compact();
	}

	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	@SuppressWarnings("unchecked")
	public List<String> extractAuthorities(String token) {
		Object value = parseClaims(token).get("authorities");
		return value instanceof List<?> l ? (List<String>) l : List.of();
	}

	private Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}
}


