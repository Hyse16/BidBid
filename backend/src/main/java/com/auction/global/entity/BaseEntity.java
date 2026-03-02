package com.auction.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티의 공통 부모 클래스
 *
 * JPA Auditing을 통해 생성일시(createdAt)와 수정일시(updatedAt)를 자동으로 관리한다.
 * 모든 엔티티는 이 클래스를 상속받아 공통 시간 필드를 재사용한다.
 *
 * - createdAt : 엔티티 최초 저장 시 자동 세팅 (이후 변경 불가)
 * - updatedAt : 엔티티 수정 시 자동 갱신
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false) // 생성 이후 변경 금지
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
