package com.auction.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JPA 및 QueryDSL 설정
 *
 * QueryDSL을 사용하여 타입 안전한 동적 쿼리를 작성한다.
 * 경매 목록 조회 시 카테고리 필터링, 상태 필터링, 페이지네이션에 활용된다.
 *
 * JPAQueryFactory는 EntityManager를 주입받아 생성하며,
 * 스레드별로 독립적인 EntityManager를 사용하므로 빈으로 등록해도 안전하다.
 */
@Configuration
public class JpaConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /** QueryDSL 쿼리 생성 팩토리 - Repository에서 주입받아 사용 */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
