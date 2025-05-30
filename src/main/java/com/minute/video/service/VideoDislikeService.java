package com.minute.video.service;

import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoDislike;
import com.minute.video.dto.VideoDislikesResponseDTO;
import com.minute.video.repository.VideoDislikeRepository;
import com.minute.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoDislikeService {
    private final VideoDislikeRepository dislikeRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    public void toggleDislike(String userId, String videoId) {
        // 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        if (!videoRepository.existsById(videoId)) {
            throw new ResourceNotFoundException("Video", videoId);
        }

        // 이미 싫어요 되어 있으면 삭제
        if (dislikeRepository.existsByUserUserIdAndVideoVideoId(userId, videoId)) {
            dislikeRepository.deleteByUserUserIdAndVideoVideoId(userId, videoId);
        } else {
            // 싫어요 저장
            User user = userRepository.getReferenceById(userId);
            Video video = videoRepository.getReferenceById(videoId);

            dislikeRepository.save(VideoDislike.builder()
                    .user(user)
                    .video(video)
                    .createdAt(java.time.LocalDateTime.now())
                    .build());
        }
    }

    public List<VideoDislikesResponseDTO> getUserDislikedVideos(String userId) {
        return dislikeRepository.findByUserUserId(userId).stream()
                .map(d -> VideoDislikesResponseDTO.builder()
                        .videoId(d.getVideo().getVideoId())
                        .videoTitle(d.getVideo().getVideoTitle())
                        .videoUrl(d.getVideo().getVideoUrl())
                        .thumbnailUrl(d.getVideo().getThumbnailUrl())
                        .createdAt(d.getCreatedAt())
                        .build())
                .toList();
    }

    // 커스텀 예외
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String resource, String id) {
            super(resource + " not found with ID: " + id);
        }
    }

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }
}
