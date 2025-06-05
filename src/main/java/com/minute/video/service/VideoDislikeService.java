package com.minute.video.service;

import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoDislike;
import com.minute.video.dto.VideoDislikesResponseDTO;
import com.minute.video.repository.VideoDislikeRepository;
import com.minute.video.repository.VideoLikesRepository;
import com.minute.video.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoDislikeService {
    private final VideoDislikeRepository dislikeRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final VideoLikesRepository likesRepository;

    @Transactional
    public void toggleDislike(String userId, String videoId) {
        // (1) user/video 존재 체크
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        if (!videoRepository.existsById(videoId)) {
            throw new ResourceNotFoundException("Video", videoId);
        }

        boolean alreadyDisliked = dislikeRepository.existsByUserUserIdAndVideoVideoId(userId, videoId);
        boolean alreadyLiked    = likesRepository.existsByUserUserIdAndVideoVideoId(userId, videoId);

        if (alreadyDisliked) {
            // ── “이미 dislike가 있으면” → 단순히 삭제
            dislikeRepository.deleteByUserUserIdAndVideoVideoId(userId, videoId);

        } else {
            // ── “새롭게 dislike 등록 전”에,
            //     (2) 만약 기존에 like 레코드가 있으면 먼저 삭제
            if (alreadyLiked) {
                likesRepository.deleteByUserUserIdAndVideoVideoId(userId, videoId);
                // 좋아요 수 감소(선택)
                Video v = videoRepository.getReferenceById(videoId);
                v.decreaseLikes();
            }

            // ── 그 후에 디스라이크 저장
            User user   = userRepository.getReferenceById(userId);
            Video video = videoRepository.getReferenceById(videoId);

            dislikeRepository.save(
                    VideoDislike.builder()
                            .user(user)
                            .video(video)
                            .createdAt(java.time.LocalDateTime.now())
                            .build()
            );
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