package com.minute.common.file.service; // 또는 com.minute.board.qna.service

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface FileStorageService {

    /**
     * 단일 파일을 업로드합니다.
     *
     * @param file         업로드할 MultipartFile
     * @param subDirectory 저장할 하위 디렉토리 경로 (예: "qna", "profile")
     * @return 저장된 파일의 접근 URL 또는 Key
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    String uploadFile(MultipartFile file, String subDirectory) throws IOException;

    /**
     * 여러 파일을 업로드합니다.
     *
     * @param files        업로드할 MultipartFile 목록
     * @param subDirectory 저장할 하위 디렉토리 경로
     * @return 저장된 파일들의 접근 URL 또는 Key 목록
     */
    List<String> uploadFiles(List<MultipartFile> files, String subDirectory);

    /**
     * 저장된 파일을 삭제합니다.
     *
     * @param fileKey S3 객체 키 또는 전체 URL (구현에 따라 다름)
     */
    void deleteFile(String fileKey);

    /**
     * 파일 키(S3 Object Key)를 기반으로 완전한 접근 URL을 생성합니다.
     * (S3 구현체에서는 보통 파일 업로드 시 바로 전체 URL을 반환하는 경우가 많습니다.)
     *
     * @param fileKey S3 객체 키
     * @return 파일 접근 URL
     */
    String getFileUrl(String fileKey);
}