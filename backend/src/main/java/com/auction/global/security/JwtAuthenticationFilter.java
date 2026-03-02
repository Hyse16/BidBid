package com.auction.global.security;

import com.auction.global.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 *
 * 모든 HTTP 요청에서 Authorization 헤더를 확인하여 JWT 토큰을 검증하고,
 * 유효한 경우 SecurityContext에 인증 정보를 등록한다.
 *
 * OncePerRequestFilter를 상속받아 요청당 정확히 한 번만 실행된다.
 *
 * 처리 흐름:
 *   1. Authorization: Bearer {token} 헤더에서 토큰 추출
 *   2. 토큰 유효성 검증 (JwtTokenProvider)
 *   3. Authentication 객체 생성 → SecurityContext 등록
 *   4. 토큰 없음 / 검증 실패 → SecurityContext 비워두고 다음 필터로 통과
 *      (인증 필요 경로는 이후 Security 설정에서 거부)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX        = "Bearer ";       // 접두사 길이: 7

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    // 토큰이 유효하면 Authentication 객체를 SecurityContext에 등록
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (CustomException e) {
                // 토큰 검증 실패 시 SecurityContext 초기화 (익명 사용자로 처리)
                log.warn("JWT authentication failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     * "Bearer " 접두사를 제거한 순수 토큰 문자열을 반환한다.
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
