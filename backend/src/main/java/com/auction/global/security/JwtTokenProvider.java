package com.auction.global.security;

import com.auction.global.exception.CustomException;
import com.auction.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 발급 및 검증 컴포넌트
 *
 * Access Token과 Refresh Token을 생성하고 검증하는 역할을 담당한다.
 *
 * 토큰 구조:
 *   subject   : 사용자 이메일
 *   issuedAt  : 발급 시각
 *   expiration: 만료 시각
 *   signature : HMAC-SHA256 (secretKey 기반)
 *
 * 만료 시간 (application.yml):
 *   Access Token  : 1시간  (3,600,000ms)
 *   Refresh Token : 7일    (604,800,000ms)
 */
@Slf4j
@Component
public class JwtTokenProvider {

    // ── @Value 주입 필드 ─────────────────────────────────────────────────────────
    @Value("${jwt.secret}")
    private String    secretKeyString; // 서명에 사용할 비밀 키 (최소 32자 이상 권장)

    @Value("${jwt.access-expiration}")
    private long      accessExpiration;  // Access Token 만료 시간 (ms)

    @Value("${jwt.refresh-expiration}")
    private long      refreshExpiration; // Refresh Token 만료 시간 (ms)

    // ── 내부 상태 필드 ────────────────────────────────────────────────────────────
    private SecretKey secretKey; // @PostConstruct에서 초기화

    private final CustomUserDetailsService userDetailsService;

    public JwtTokenProvider(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /** 빈 초기화 후 secretKeyString을 SecretKey 객체로 변환 */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    // ── 토큰 발급 ─────────────────────────────────────────────────────────────────

    /** Access Token 발급 */
    public String createAccessToken(String email) {
        return createToken(email, accessExpiration);
    }

    /** Refresh Token 발급 */
    public String createRefreshToken(String email) {
        return createToken(email, refreshExpiration);
    }

    /** 공통 토큰 생성 로직 */
    private String createToken(String subject, long expiration) {
        Date now        = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(subject)       // 이메일을 subject로 저장
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)    // HMAC-SHA256 서명
                .compact();
    }

    // ── 토큰 파싱 / 검증 ──────────────────────────────────────────────────────────

    /**
     * 토큰에서 Authentication 객체 생성
     * SecurityContext에 등록하여 @AuthenticationPrincipal 등으로 사용자 정보를 조회한다.
     */
    public Authentication getAuthentication(String token) {
        String      email       = getEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /** 토큰에서 이메일(subject) 추출 */
    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰 유효성 검증
     *
     * 만료         → TOKEN_EXPIRED 예외 (클라이언트가 Refresh Token으로 재발급 요청 가능)
     * 위변조 등    → TOKEN_INVALID 예외
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    /** 토큰 파싱 및 Claims 추출 */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
