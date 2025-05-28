package com.minute.video.service;

import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoDislike;
import com.minute.video.repository.VideoDislikeRepository;
import com.minute.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoDislikeService {
    private final VideoDislikeRepository dislikeRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    public void toggleDislike(String userId, String videoId) {
        if (dislikeRepository.existsByUserUserIdAndVideoVideoId(userId, videoId)) {
            dislikeRepository.deleteByUserUserIdAndVideoVideoId(userId, videoId);
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("Video not found"));
            dislikeRepository.save(VideoDislike.builder()
                    .user(user)
                    .video(video)
                    .build());
        }
    }
}
