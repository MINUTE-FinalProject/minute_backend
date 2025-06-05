package com.minute.common.file.service.implement; // 또는 com.minute.board.qna.service.implement

import com.minute.common.file.service.FileStorageService; // 인터페이스 경로
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service("s3FileStorageService") // 빈 이름 지정
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    // S3Client는 AWSConfig.java 와 같은 설정 클래스에서 Bean으로 주입받습니다.
    public S3FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(MultipartFile file, String subDirectory) throws IOException {
        if (file == null || file.isEmpty()) {
            return null; // 또는 예외 처리
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        String s3Key = (subDirectory != null && !subDirectory.isEmpty() ? subDirectory.replaceAll("^/+", "").replaceAll("/+$", "") + "/" : "") + uniqueFilename;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
//                .acl(ObjectCannedACL.PUBLIC_READ) // 필요에 따라 ACL 설정 (예: 공개 읽기 가능)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        log.info("File uploaded to S3: bucket={}, key={}", bucketName, s3Key);

        // 업로드된 파일의 URL 반환
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(s3Key)).toExternalForm();
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String subDirectory) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        return files.stream()
                .map(file -> {
                    try {
                        return uploadFile(file, subDirectory);
                    } catch (IOException e) {
                        log.error("Error uploading file to S3: {}", file.getOriginalFilename(), e);
                        // 개별 파일 업로드 실패 시 null을 반환하거나, 예외를 던지거나, 실패 목록을 별도로 관리할 수 있습니다.
                        // 여기서는 null을 반환하고 나중에 filter(Objects::nonNull) 등으로 처리할 수 있습니다.
                        return null;
                    }
                })
                .filter(url -> url != null && !url.isEmpty()) // 실패한(null) 업로드는 제외
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFile(String fileUrlOrKey) {
        if (fileUrlOrKey == null || fileUrlOrKey.isEmpty()) {
            log.warn("File URL or Key is null or empty, cannot delete.");
            return;
        }

        String keyToDelete;
        // 전달받은 것이 전체 URL인지, 아니면 S3 키인지 판단
        // 가장 간단한 방법은 URL에서 버킷 이름과 키를 파싱하는 것입니다.
        // 예: "https://<bucket-name>.s3.<region>.amazonaws.com/<key>"
        try {
            URL url = new URL(fileUrlOrKey);
            String path = url.getPath(); // path는 "/<key>" 형태
            if (path.startsWith("/")) {
                keyToDelete = path.substring(1); // 첫번째 '/' 제거
            } else {
                keyToDelete = path;
            }
            // 추가 검증: URL이 현재 버킷의 URL 패턴과 맞는지 확인
            if (!fileUrlOrKey.contains(bucketName)) {
                log.warn("The file URL {} does not seem to belong to the configured bucket {}. Attempting delete with extracted key: {}", fileUrlOrKey, bucketName, keyToDelete);
                // 만약 fileKey만 전달받는 경우라면 바로 사용
                // keyToDelete = fileUrlOrKey;
            }

        } catch (Exception e) {
            // URL 파싱 실패 시, fileUrlOrKey 자체가 key라고 가정
            log.warn("Could not parse URL, assuming fileUrlOrKey is an S3 key: {}", fileUrlOrKey, e);
            keyToDelete = fileUrlOrKey;
        }

        if (keyToDelete == null || keyToDelete.isEmpty()) {
            log.error("Could not determine S3 key from: {}", fileUrlOrKey);
            return;
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(keyToDelete)
                .build();
        try {
            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted from S3: bucket={}, key={}", bucketName, keyToDelete);
        } catch (Exception e) {
            log.error("Error deleting file from S3: bucket={}, key={}", bucketName, keyToDelete, e);
            // 필요한 경우 예외를 다시 던지거나 처리
        }
    }

    @Override
    public String getFileUrl(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return null;
        }
        try {
            return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileKey)).toExternalForm();
        } catch (Exception e) {
            log.error("Error generating S3 URL for key {}: {}", fileKey, e.getMessage());
            return null; // 또는 기본 이미지 URL 반환
        }
    }
}