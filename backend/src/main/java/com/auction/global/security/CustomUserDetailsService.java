package com.auction.global.security;

import com.auction.global.exception.CustomException;
import com.auction.global.exception.ErrorCode;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security의 UserDetailsService 구현체
 *
 * JWT 토큰에서 추출한 이메일로 사용자를 조회하여 UserDetails를 반환한다.
 * JwtTokenProvider → CustomUserDetailsService → UserRepository 순으로 호출된다.
 *
 * 권한은 "ROLE_{ROLE명}" 형식으로 등록한다.
 *   예) USER → ROLE_USER, ADMIN → ROLE_ADMIN
 *
 * @Lazy 주의:
 *   SecurityConfig → JwtAuthenticationFilter → JwtTokenProvider → CustomUserDetailsService
 *   → UserRepository 순의 의존 체인에서 순환 의존이 발생할 수 있어
 *   UserRepository를 @Lazy로 주입하여 초기화 시점을 늦춘다.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final com.auction.domain.user.repository.UserRepository userRepository;

    // SecurityConfig와의 순환 의존 방지를 위해 @Lazy 사용
    public CustomUserDetailsService(
            @Lazy com.auction.domain.user.repository.UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 이메일로 사용자를 조회하여 UserDetails 반환
     * 사용자가 없으면 USER_NOT_FOUND 예외 발생
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.auction.domain.user.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }

    /**
     * 이메일로 도메인 User 엔티티를 직접 조회한다.
     *
     * 컨트롤러에서 @AuthenticationPrincipal UserDetails → User 엔티티 변환 시 사용한다.
     * UserDetails는 Spring Security 객체이므로 도메인 로직에 사용하려면 엔티티 변환이 필요하다.
     */
    public com.auction.domain.user.entity.User loadUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
