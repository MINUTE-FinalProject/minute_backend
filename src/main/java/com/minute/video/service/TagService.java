package com.minute.video.service;

import com.minute.video.Entity.Video;
import com.minute.video.dto.TagResponseDTO;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.mapper.VideoMapper;
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
    private final VideoMapper videoMapper;
    // 모든 태그 조회
    public List<TagResponseDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> new TagResponseDTO(tag.getTagId(), tag.getTagName()))
                .collect(Collectors.toList());
    }

}
