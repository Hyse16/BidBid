package com.auction.domain.user.repository;

import com.auction.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 레포지토리
 *
 * Spring Data JPA가 런타임에 구현체를 자동 생성한다.
 * 기본 CRUD는 JpaRepository가 제공하며, 아래는 도메인에 특화된 쿼리 메서드를 추가한다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 이메일로 사용자 조회 (로그인, JWT 인증 시 사용) */
    Optional<User> findByEmail(String email);

    /** 이메일 중복 확인 (회원가입 시 사용) */
    boolean existsByEmail(String email);

    /** 닉네임 중복 확인 (회원가입 시 사용) */
    boolean existsByNickname(String nickname);
}
