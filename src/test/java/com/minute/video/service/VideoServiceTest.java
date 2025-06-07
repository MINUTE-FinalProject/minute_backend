package com.minute.video.service;

import com.minute.bookmark.repository.BookmarkRepository;
import com.minute.video.Entity.*;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.mapper.VideoResponseMapper;
import com.minute.video.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VideoServiceTest {

    @Mock VideoRepository videoRepository;
    @Mock WatchHistoryRepository watchHistoryRepository;
    @Mock VideoLikesRepository videoLikesRepository;
    @Mock SearchHistoryRepository searchHistoryRepository;
    @Mock BookmarkRepository bookmarkRepository;
    @Mock VideoResponseMapper videoResponseMapper;
    @Mock VideoFilterService videoFilterService;
    @InjectMocks VideoService videoService;

    Video v1, v2, v3;

    @BeforeEach
    void setUp() {
        v1 = Video.builder().videoId("A").videoTitle("Vid A").build();
        v2 = Video.builder().videoId("B").videoTitle("Vid B").build();
        v3 = Video.builder().videoId("C").videoTitle("Vid C").build();

        // mapper 스텁
        when(videoResponseMapper.toDtoWithStats(any(Video.class), anyInt()))
                .thenAnswer(inv -> {
                    Video vid = inv.getArgument(0);
                    int score = inv.getArgument(1);
                    return VideoResponseDTO.builder()
                            .videoId(vid.getVideoId())
                            .videoTitle(vid.getVideoTitle())
                            .recommendationScore(score)
                            .build();
                });
    }

    @Test
    void 추천_영상은_점수_내림차순으로_정렬된다() {
        String userId = "userA";

        // 1) 후보
        when(videoRepository.findTop50ByOrderByViewsDesc())
                .thenReturn(List.of(v1, v2, v3));
        when(videoRepository.findTop50ByOrderByLikesDesc())
                .thenReturn(List.of());

        // 2) 시청 이력: B
        WatchHistory wh = mock(WatchHistory.class);
        when(wh.getVideo()).thenReturn(v2);
        when(watchHistoryRepository
                .findByUserUserIdOrderByWatchedAtDesc(userId))
                .thenReturn(List.of(wh));

        // buildWatchedCategoryCount 의 findAllById 스텁
        when(videoRepository.findAllById(List.of("B")))
                .thenReturn(List.of(v2));

        // 3) 좋아요: A
        VideoLikes likeA = mock(VideoLikes.class);
        when(likeA.getVideo()).thenReturn(v1);
        when(videoLikesRepository.findByUserUserId(userId))
                .thenReturn(List.of(likeA));

        // 4) 검색 이력: C
        SearchHistory sh = mock(SearchHistory.class);
        when(sh.getKeyword()).thenReturn("C");
        when(searchHistoryRepository
                .findByUserUserIdOrderBySearchedAtDesc(userId))
                .thenReturn(List.of(sh));

        // 5) 북마크: C
        when(bookmarkRepository.findVideoIdsByUserId(userId))
                .thenReturn(List.of("C"));

        // 실행
        List<VideoResponseDTO> result = videoService.getRecommendedVideos(userId);

        // 검증
        assertThat(result).extracting(VideoResponseDTO::getVideoId)
                .containsExactly("C", "A");
        assertThat(result).extracting(VideoResponseDTO::getRecommendationScore)
                .containsExactly(6, 5);
    }

    @Test
    void 추천_개수_부족하면_조회수_상위로_채워넣는다() {
        String userId = "userA";

        // 후보 영상: v1만 스코어링 대상으로
        when(videoRepository.findTop50ByOrderByViewsDesc())
                .thenReturn(List.of(v1))        // 첫 호출: 후보
                .thenReturn(List.of(v2, v3));   // 두 번째 호출: 채워넣기용

        // 좋아요/조회수 top50 합친 후보(v1) 외엔 없음
        when(videoRepository.findTop50ByOrderByLikesDesc()).thenReturn(List.of());

        // 아무 것도 좋아요·검색·북마크·시청 이력 없음 → 점수 모두 0
        when(watchHistoryRepository.findByUserUserIdOrderByWatchedAtDesc(userId))
                .thenReturn(List.of());
        when(videoLikesRepository.findByUserUserId(userId))
                .thenReturn(List.of());
        when(searchHistoryRepository.findByUserUserIdOrderBySearchedAtDesc(userId))
                .thenReturn(List.of());
        when(bookmarkRepository.findVideoIdsByUserId(userId))
                .thenReturn(List.of());

        // 카테고리 보정, 필터링 서비스 전부 기본값 처리
        when(videoFilterService.isAllowed(any(), anyString())).thenReturn(true);

        // mapper 스텁 (score 없이 title만 매핑)
        // — 이미 @BeforeEach에서 걸어두셨으니 생략해도 됩니다

        // 실행
        List<VideoResponseDTO> result = videoService.getRecommendedVideos(userId);

        // 초기 후보(v1) + filler(v2, v3) → 총 3개
        assertThat(result).extracting(VideoResponseDTO::getVideoId)
                .containsExactly("A", "B", "C");

        // 점수는 모두 0점
        assertThat(result).extracting(VideoResponseDTO::getRecommendationScore)
                .containsExactly(0, 0, 0);
    }


    @Test
    void 추천_영상이_없으면_빈_리스트를_반환한다() {
        String userId = "userA";

        // 1) 후보 리스트: 조회수/좋아요 top50 대신 간단히 v1, v2, v3
        when(videoRepository.findTop50ByOrderByViewsDesc())
                .thenReturn(List.of(v1, v2, v3));
        when(videoRepository.findTop50ByOrderByLikesDesc())
                .thenReturn(List.of());

        // 2) 시청 이력: v1, v2, v3 모두 이미 본 것으로 설정
        WatchHistory wh1 = mock(WatchHistory.class);
        WatchHistory wh2 = mock(WatchHistory.class);
        WatchHistory wh3 = mock(WatchHistory.class);
        when(wh1.getVideo()).thenReturn(v1);
        when(wh2.getVideo()).thenReturn(v2);
        when(wh3.getVideo()).thenReturn(v3);
        when(watchHistoryRepository.findByUserUserIdOrderByWatchedAtDesc(userId))
                .thenReturn(List.of(wh1, wh2, wh3));

        // buildWatchedCategoryCount 호출 시에도 빈 리스트 반환하도록 스텁
        when(videoRepository.findAllById(List.of("A", "B", "C")))
                .thenReturn(List.of(v1, v2, v3));

        // 3) 나머지 이력/좋아요/검색/북마크는 모두 빈 리스트
        when(videoLikesRepository.findByUserUserId(userId)).thenReturn(List.of());
        when(searchHistoryRepository.findByUserUserIdOrderBySearchedAtDesc(userId))
                .thenReturn(List.of());
        when(bookmarkRepository.findVideoIdsByUserId(userId)).thenReturn(List.of());

        // 실행
        List<VideoResponseDTO> result = videoService.getRecommendedVideos(userId);

        // 검증: 빈 리스트여야 함
        assertThat(result).isEmpty();
    }

    @Test
    void 후보_없으면_빈_리스트() {
        when(videoRepository.findTop50ByOrderByViewsDesc()).thenReturn(List.of());
        when(videoRepository.findTop50ByOrderByLikesDesc()).thenReturn(List.of());
        List<VideoResponseDTO> result = videoService.getRecommendedVideos("any");
        assertThat(result).isEmpty();
    }

    @Test
    void 카테고리_보정점수_적용된다() {
        String userId = "userA";

        // 1) 후보 영상(v1) 세팅
        when(videoRepository.findTop50ByOrderByViewsDesc()).thenReturn(List.of(v1));
        when(videoRepository.findTop50ByOrderByLikesDesc()).thenReturn(List.of());

        // 2) 시청 이력: v2
        WatchHistory wh = mock(WatchHistory.class);
        when(wh.getVideo()).thenReturn(v2);
        when(watchHistoryRepository
                .findByUserUserIdOrderByWatchedAtDesc(userId))
                .thenReturn(List.of(wh));

        // 3) watchedCategoryCount 내부 조회를 위한 findAllById 스텁
        //    → v2에도 캠핑 카테고리가 있어야 함
        Category camping = Category.builder().categoryName("캠핑").build();
        VideoCategory vc2 = VideoCategory.builder()
                .video(v2)
                .category(camping)
                .build();
        v2.setVideoCategories(List.of(vc2));
        when(videoRepository.findAllById(List.of("B")))
                .thenReturn(List.of(v2));

        // 4) 추천 후보인 v1에도 같은 캠핑 카테고리를 붙여야 +1점
        VideoCategory vc1 = VideoCategory.builder()
                .video(v1)
                .category(camping)
                .build();
        v1.setVideoCategories(List.of(vc1));

        // 5) 그 외 스텁(좋아요·검색·북마크 모두 없음)
        when(videoLikesRepository.findByUserUserId(userId))
                .thenReturn(List.of());
        when(searchHistoryRepository
                .findByUserUserIdOrderBySearchedAtDesc(userId))
                .thenReturn(List.of());
        when(bookmarkRepository.findVideoIdsByUserId(userId))
                .thenReturn(List.of());

        // 실행
        List<VideoResponseDTO> result = videoService.getRecommendedVideos(userId);

        // 검증: 캠핑 카테고리 보정 +1만 적용
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecommendationScore()).isEqualTo(1);
    }


    @Test
    void 검색_북마크_좋아요_점수_가중치_각각_적용된다() {
        String userId = "userA";

        // ─ 후보 영상 세 개 (v1, v2, v3)
        when(videoRepository.findTop50ByOrderByViewsDesc()).thenReturn(List.of(v1, v2, v3));
        when(videoRepository.findTop50ByOrderByLikesDesc()).thenReturn(List.of());

        // 시청 이력 비어있음
        when(watchHistoryRepository.findByUserUserIdOrderByWatchedAtDesc(userId))
                .thenReturn(List.of());

        // 빌드된 watchedCategoryCount 비어있도록
        when(videoRepository.findAllById(any())).thenReturn(List.of());

        // ─ 좋아요: v1 → +5
        VideoLikes like1 = mock(VideoLikes.class);
        when(like1.getVideo()).thenReturn(v1);
        when(videoLikesRepository.findByUserUserId(userId)).thenReturn(List.of(like1));

        // ─ 검색 키워드: “Vid B” 포함 → v2 +2
        SearchHistory sh = mock(SearchHistory.class);
        when(sh.getKeyword()).thenReturn("Vid B");
        when(searchHistoryRepository.findByUserUserIdOrderBySearchedAtDesc(userId))
                .thenReturn(List.of(sh));

        // ─ 북마크: v3 → +4
        when(bookmarkRepository.findVideoIdsByUserId(userId))
                .thenReturn(List.of("C"));

        // 카테고리/필터 무시
        // 실행
        List<VideoResponseDTO> result = videoService.getRecommendedVideos(userId);

        // 각 점수 확인
        // v1: 좋아요(5) → 5
        // v2: 검색(2) → 2
        // v3: 북마크(4) → 4
        assertThat(result).extracting(VideoResponseDTO::getVideoId)
                .containsExactly("A","C","B");  // 5점,4점,2점 순
        assertThat(result).extracting(VideoResponseDTO::getRecommendationScore)
                .containsExactly(5,4,2);
    }


}
