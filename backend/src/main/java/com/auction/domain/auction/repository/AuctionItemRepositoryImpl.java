package com.auction.domain.auction.repository;

import com.auction.domain.auction.dto.AuctionListResponse;
import com.auction.domain.auction.dto.AuctionSearchCondition;
import com.auction.domain.auction.entity.AuctionItem.AuctionStatus;
import com.auction.domain.auction.entity.QAuctionImage;
import com.auction.domain.auction.entity.QAuctionItem;
import com.auction.domain.category.entity.QCategory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * QueryDSL 커스텀 레포지토리 구현체
 *
 * 카테고리 / 상태 / 키워드(제목 부분 일치) 기반 동적 검색을 처리한다.
 * 목록 조회 시 썸네일 이미지 URL을 LEFT JOIN으로 함께 조회하여
 * 이미지가 없는 경우에도 경매 항목이 정상 노출된다.
 *
 * Q 클래스 사용 전 반드시 gradle compileJava 실행 필요:
 *   ./gradlew compileJava → build/generated/querydsl/ 에 Q 클래스 생성
 */
@RequiredArgsConstructor
public class AuctionItemRepositoryImpl implements AuctionItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AuctionListResponse> searchAuctions(AuctionSearchCondition condition, Pageable pageable) {
        QAuctionItem  qItem     = QAuctionItem.auctionItem;
        QAuctionImage qImage    = QAuctionImage.auctionImage;
        QCategory     qCategory = QCategory.category;

        // 동적 조건 빌드 (null인 조건은 자동 제외)
        BooleanBuilder where = buildCondition(condition, qItem);

        // 목록 데이터 조회: 썸네일 이미지 LEFT JOIN
        List<AuctionListResponse> content = queryFactory
                .select(Projections.constructor(AuctionListResponse.class,
                        qItem.id,
                        qItem.title,
                        qItem.currentPrice,
                        qItem.buyNowPrice,
                        qItem.status.stringValue(),
                        qItem.endAt,
                        qCategory.name,
                        qImage.imageUrl))
                .from(qItem)
                .join(qItem.category, qCategory)
                .leftJoin(qItem.images, qImage).on(qImage.isThumbnail.isTrue())
                .where(where)
                .orderBy(qItem.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회 (페이징 메타정보 생성용)
        Long total = queryFactory
                .select(qItem.count())
                .from(qItem)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /** 검색 조건을 동적으로 조합한다 (null 조건은 자동 스킵) */
    private BooleanBuilder buildCondition(AuctionSearchCondition condition, QAuctionItem qItem) {
        BooleanBuilder builder = new BooleanBuilder();

        // 카테고리 필터
        if (condition.categoryId() != null) {
            builder.and(qItem.category.id.eq(condition.categoryId()));
        }

        // 상태 필터 (문자열 → enum 변환)
        if (StringUtils.hasText(condition.status())) {
            builder.and(qItem.status.eq(AuctionStatus.valueOf(condition.status())));
        }

        // 키워드 검색: 제목에 포함된 경우 (대소문자 무시)
        if (StringUtils.hasText(condition.keyword())) {
            builder.and(qItem.title.containsIgnoreCase(condition.keyword()));
        }

        return builder;
    }
}
