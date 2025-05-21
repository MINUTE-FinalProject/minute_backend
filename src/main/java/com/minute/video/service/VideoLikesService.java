package com.minute.video.service;

import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoLikes;
import com.minute.video.dto.SearchHistoryResponseDTO;
import com.minute.video.dto.VideoLikesRequestDTO;
import com.minute.video.dto.VideoLikesResponseDTO;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.mapper.VideoMapper;
import com.minute.video.repository.VideoLikesRepository;
import com.minute.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoLikesService {
    // 좋아요 영상 저장 및 조회

    private final VideoLikesRepository videoLikesRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;

    // 좋아요 저장
    public void savelike(VideoLikesRequestDTO videoLikesRequestDTO) {
        // User 객체 조회
        User user = userRepository.findById(videoLikesRequestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + videoLikesRequestDTO.getUserId()));

        // Video 객체 조회
        Video video = videoRepository.findById(videoLikesRequestDTO.getVideoId())
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + videoLikesRequestDTO.getVideoId()));

        // 중복 체크
        boolean alreadyLiked = videoLikesRepository.existsByUserAndVideo(user, video);
        if (alreadyLiked) {
            throw new IllegalStateException("User already liked this video");
        }

        VideoLikes videoLikes = VideoLikes.builder()
                .user(user)
                .video(video)
                .build();
        videoLikesRepository.save(videoLikes);
    }

    // 사용자가 좋아요한 영상 목록 조회
    public List<VideoLikesResponseDTO> getUserLikedVideos(String userId) {
        // User 객체 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return videoLikesRepository.findByUserUserId(userId).stream()
                .map(videoLikes -> new VideoLikesResponseDTO(
                        videoLikes.getVideo().getVideoId(),
                        videoLikes.getVideo().getVideoTitle(),
                        videoLikes.getVideo().getVideoUrl(),
                        videoLikes.getVideo().getThumbnailUrl()))
                .collect(Collectors.toList());
    }
}
