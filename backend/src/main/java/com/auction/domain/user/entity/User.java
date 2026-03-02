package com.auction.domain.user.entity;

import com.auction.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 엔티티 (users 테이블)
 *
 * 경매 플랫폼의 회원 정보를 나타낸다.
 * 비밀번호는 BCrypt로 암호화하여 저장하며, 평문 비밀번호는 절대 저장하지 않는다.
 * telegramChatId는 선택 항목으로, 텔레그램 알림을 받으려는 사용자만 등록한다.
 *
 * Lombok 전략:
 *   @NoArgsConstructor(PROTECTED) - JPA 프록시용 기본 생성자, 외부 직접 생성 차단
 *   @Builder                      - 객체 생성은 빌더 패턴으로만 허용
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;           // 로그인 ID로 사용, 중복 불가

    @Column(nullable = false)
    private String password;        // BCrypt 암호화 저장

    @Column(nullable = false, length = 50)
    private String nickname;        // 화면에 표시되는 이름

    @Column(length = 100)
    private String telegramChatId;  // 텔레그램 알림 수신용 Chat ID (선택)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;  // 기본 권한은 일반 사용자

    /** 프로필 업데이트: 닉네임과 텔레그램 Chat ID만 변경 가능 */
    public void updateProfile(String nickname, String telegramChatId) {
        this.nickname        = nickname;
        this.telegramChatId  = telegramChatId;
    }

    /** 비밀번호 변경 (외부에서 이미 암호화된 값을 전달해야 함) */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /** 사용자 권한 */
    public enum Role {
        USER,   // 일반 사용자
        ADMIN   // 관리자
    }
}
