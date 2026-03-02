package com.auction.global.config;

import com.auction.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 설정
 *
 * JWT 기반 무상태(Stateless) 인증을 사용하므로 세션을 생성하지 않는다.
 * 모든 요청은 JwtAuthenticationFilter를 통과하며,
 * 유효한 토큰이 있는 경우 SecurityContext에 인증 정보를 등록한다.
 *
 * 공개 접근 허용 경로:
 *   - /api/auth/**                  : 로그인, 회원가입, 토큰 재발급
 *   - GET /api/auctions/**          : 경매 목록 및 상세 조회 (비로그인도 가능)
 *   - /ws/**                        : WebSocket 엔드포인트
 *   - /actuator/health, /prometheus : 헬스체크 및 모니터링
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)          // REST API → CSRF 불필요
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 미사용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()                     // 인증 API 공개
                        .requestMatchers(HttpMethod.GET, "/api/auctions/**").permitAll() // 경매 조회 공개
                        .requestMatchers("/ws/**").permitAll()                           // WebSocket 공개
                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll() // 모니터링 공개
                        .anyRequest().authenticated()                                    // 나머지는 인증 필요
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 비밀번호 암호화: BCrypt 사용 (단방향 해시) */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 설정
     *
     * 로컬 개발(localhost:3000)과 프로덕션 도메인을 허용한다.
     * allowCredentials=true 이므로 쿠키/Authorization 헤더 전송이 가능하다.
     * maxAge=3600 → 브라우저가 1시간 동안 Preflight 요청을 캐싱한다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용 출처 / 메서드 / 헤더
        config.setAllowedOriginPatterns(List.of("http://localhost:3000", "https://*.your-domain.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        // 자격증명 허용 및 Preflight 캐시 시간
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
