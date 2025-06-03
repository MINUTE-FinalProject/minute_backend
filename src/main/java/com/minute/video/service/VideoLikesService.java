package com.minute.video.service;

import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoLikes;
import com.minute.video.dto.VideoLikesResponseDTO;
import com.minute.video.repository.VideoLikesRepository;
import com.minute.video.repository.VideoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoLikesService {
    // 좋아요 영상 저장 및 조회

    private final VideoLikesRepository videoLikesRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    /**
     * 좋아요 저장
     */
    public void saveLike(String userId, String videoId) {
        // 1) 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        if (!videoRepository.existsById(videoId)) {
            throw new ResourceNotFoundException("Video", videoId);
        }

        // 2) 중복 체크
        if (videoLikesRepository.existsByUserUserIdAndVideoVideoId(userId, videoId)) {
            throw new BadRequestException("User already liked this video");
        }

        // 3) 저장
        User user = userRepository.getReferenceById(userId);
        Video video = videoRepository.getReferenceById(videoId);

        VideoLikes like = VideoLikes.builder()
                .user(user)   // 또는 User 레퍼런스로만 세팅
                .video(video)
                .build();
        videoLikesRepository.save(like);


        // 좋아요 수 증가
        video.increaseLikes();
    }

    /**
     * 좋아요 삭제
     */
    public void deleteLike(String userId, String videoId) {
        // 1) 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        if (!videoRepository.existsById(videoId)) {
            throw new ResourceNotFoundException("Video", videoId);
        }

        // 2) 삭제
        int deletedCount = videoLikesRepository
                .deleteByUserUserIdAndVideoVideoId(userId, videoId);

        if (deletedCount == 0) {
            throw new BadRequestException(
                    "No existing like to delete for user=" + userId + ", video=" + videoId
            );
        }
        // 좋아요 수 감소
        Video video = videoRepository.getReferenceById(videoId); // 🔁 삭제 후라도 참조 가능
        video.decreaseLikes();
    }

    /**
     * 사용자가 좋아요한 영상 목록 조회
     */
    @Transactional(readOnly = true)
    public List<VideoLikesResponseDTO> getUserLikedVideos(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        return videoLikesRepository.findByUserUserId(userId).stream()
                .map(like -> new VideoLikesResponseDTO(
                        like.getVideo().getVideoId(),
                        like.getVideo().getVideoTitle(),
                        like.getVideo().getVideoUrl(),
                        like.getVideo().getThumbnailUrl(),
                        like.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // 커스텀 예외 클래스 예시
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