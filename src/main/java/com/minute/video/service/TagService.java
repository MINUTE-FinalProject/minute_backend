package com.minute.video.service;

import com.minute.video.dto.TagResponseDTO;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.repository.TagRepository;
import com.minute.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {
    // 태그 조회 및 태그 기반 영상 조회

    private final TagRepository tagRepository;
    private final VideoRepository videoRepository;

    // 모든 태그 조회
    public List<TagResponseDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> new TagResponseDTO(tag.getTagId(), tag.getTagName()))
                .collect(Collectors.toList());
    }

    // 특정 태그의 영상 목록 조회
    public List<VideoResponseDTO> getVideosByTag(String tagName) {
        return videoRepository.findByTagsName(tagName).stream()
                .map(video -> {
                    // 모든 태그 이름 추출
                    List<String> tagNames = video.getVideoTags().stream()
                            .map(videoTag -> videoTag.getTag().getTagName())
                            .collect(Collectors.toList());

                    // 모든 카테고리 이름 추출
                    List<String> categoryNames = video.getVideoCategories().stream()
                            .map(videoCategory -> videoCategory.getCategory().getCategoryName())
                            .collect(Collectors.toList());

                    return new VideoResponseDTO(
                            video.getVideoId(),
                            video.getVideoTitle(),
                            video.getVideoDescription(),
                            video.getVideoUrl(),
                            video.getThumbnailUrl(),
                            categoryNames,  // 수정된 부분: 모든 카테고리 전달
                            video.getChannel().getChannelName(),
                            tagNames  // 태그 이름 리스트로 전달
                    );
                })
                .collect(Collectors.toList());
    }
}
