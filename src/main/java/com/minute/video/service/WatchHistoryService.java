package com.minute.video.service;

import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.WatchHistory;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.dto.WatchHistoryRequestDTO;
import com.minute.video.dto.WatchHistoryResponseDTO;
import com.minute.video.mapper.VideoMapper;
import com.minute.video.repository.VideoRepository;
import com.minute.video.repository.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchHistoryService {
    // 시청 기록 저장 및 조회

    private final WatchHistoryRepository watchHistoryRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;

    // 시청기록저장
    public void saveWatchHistory(WatchHistoryRequestDTO watchHistoryRequestDTO) {

        // User 객체 조회
        User user = userRepository.findById(watchHistoryRequestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + watchHistoryRequestDTO.getUserId()));

        // Video 객체 조회
        Video video = videoRepository.findById(watchHistoryRequestDTO.getVideoId())
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + watchHistoryRequestDTO.getVideoId()));

        WatchHistory watchHistory = WatchHistory.builder()
                .user(user)
                .video(video)
                .watchedAt(watchHistoryRequestDTO.getWatchedAt())
                .build();
        watchHistoryRepository.save(watchHistory);
    }
    // 특정 사용자의 시청 기록 조회
    public List<WatchHistoryResponseDTO> getUserWatchHistory(String userId){
        // User 객체 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return watchHistoryRepository.findByUserUserIdOrderByWatchedAtDesc(userId).stream()
                .map(watchHistory -> new WatchHistoryResponseDTO(
                        watchHistory.getVideo().getVideoId(),
                        watchHistory.getVideo().getVideoTitle(),
                        watchHistory.getVideo().getVideoUrl(),
                        watchHistory.getVideo().getThumbnailUrl(),
                        watchHistory.getWatchedAt()))
                .collect(Collectors.toList());
    }
}
