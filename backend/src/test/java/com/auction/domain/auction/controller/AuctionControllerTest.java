package com.auction.domain.auction.controller;

import com.auction.domain.auction.dto.AuctionListResponse;
import com.auction.domain.auction.dto.AuctionResponse;
import com.auction.domain.auction.dto.AuctionSearchCondition;
import com.auction.domain.auction.entity.AuctionItem.AuctionStatus;
import com.auction.domain.auction.service.AuctionService;
import com.auction.domain.user.entity.User;
import com.auction.global.security.CustomUserDetailsService;
import com.auction.global.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuctionController 슬라이스 테스트 (@WebMvcTest)
 *
 * 실제 서비스 레이어 없이 MockMvc와 Mockito로 컨트롤러 계층만 검증한다.
 *   - HTTP 메서드 / URL 매핑
 *   - 응답 JSON 구조
 *   - 인증 필요 엔드포인트 접근 제어
 */
@WebMvcTest(
        controllers = AuctionController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class) // JWT 필터는 테스트에서 제외
)
class AuctionControllerTest {

    @Autowired MockMvc       mockMvc;
    @Autowired ObjectMapper  objectMapper;

    @MockBean AuctionService           auctionService;
    @MockBean CustomUserDetailsService userDetailsService;

    // ── 경매 목록 조회 테스트 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/auctions — 경매 목록을 페이징하여 조회한다")
    void getAuctions_returnsPagedList() throws Exception {
        // given
        AuctionListResponse item = new AuctionListResponse(
                1L, "맥북 프로", 1_000_000L, null, "ACTIVE",
                LocalDateTime.now().plusDays(7), "전자제품", null
        );
        Page<AuctionListResponse> page = new PageImpl<>(List.of(item));
        given(auctionService.getAuctions(any(AuctionSearchCondition.class), any(Pageable.class)))
                .willReturn(page);

        // when & then
        mockMvc.perform(get("/api/auctions")
                        .param("status", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("맥북 프로"))
                .andExpect(jsonPath("$.data.content[0].currentPrice").value(1_000_000));
    }

    @Test
    @DisplayName("GET /api/auctions/{id} — 경매 상세 정보를 조회한다")
    void getAuction_returnsDetail() throws Exception {
        // given
        AuctionResponse response = new AuctionResponse(
                1L, "맥북 프로", "M3 맥북 14인치",
                1_000_000L, 1_000_000L, null, 10_000L,
                "PENDING",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(7),
                "전자제품", "판매자", List.of()
        );
        given(auctionService.getAuction(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/auctions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("맥북 프로"))
                .andExpect(jsonPath("$.data.sellerNickname").value("판매자"))
                .andExpect(jsonPath("$.data.minBidUnit").value(10_000));
    }

    // ── 인증 필요 API 접근 제어 테스트 ────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auctions — 비인증 요청은 401을 반환한다")
    void createAuction_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/auctions")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "seller@test.com")
    @DisplayName("DELETE /api/auctions/{id} — 인증된 사용자는 삭제를 요청할 수 있다")
    void deleteAuction_authenticated_callsService() throws Exception {
        // given
        User mockUser = User.builder()
                .id(1L).email("seller@test.com").password("encoded")
                .nickname("판매자").role(User.Role.USER).build();
        given(userDetailsService.loadUserEntityByEmail("seller@test.com")).willReturn(mockUser);
        willDoNothing().given(auctionService).deleteAuction(eq(1L), any(User.class));

        // when & then
        mockMvc.perform(delete("/api/auctions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        then(auctionService).should().deleteAuction(eq(1L), any(User.class));
    }
}
