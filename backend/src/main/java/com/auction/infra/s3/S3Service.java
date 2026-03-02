package com.auction.infra.s3;

import com.auction.global.exception.CustomException;
import com.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * AWS S3 파일 업로드/삭제 서비스
 *
 * 경매 상품 이미지를 S3에 저장하고, 삭제 시 S3에서도 제거한다.
 *
 * 허용 파일 타입: JPEG, PNG, WebP
 * 최대 파일 크기: 5MB
 *
 * 파일 키 형식: {directory}/{UUID}{확장자}
 *   예) auction-images/550e8400-e29b-41d4-a716-446655440000.jpg
 *
 * 업로드된 이미지 URL 형식:
 *   https://{bucket}.s3.{region}.amazonaws.com/{key}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    // ── 주입 필드 ─────────────────────────────────────────────────────────────────
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket; // S3 버킷 이름

    @Value("${aws.region}")
    private String region; // AWS 리전 (URL 생성에 사용)

    // ── 상수 ──────────────────────────────────────────────────────────────────────
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final long         MAX_FILE_SIZE  = 5 * 1024 * 1024L; // 5MB

    /**
     * 파일을 S3에 업로드하고 접근 가능한 URL을 반환한다.
     *
     * @param file      업로드할 파일
     * @param directory S3 내 저장 디렉토리 (예: "auction-images")
     * @return 업로드된 파일의 S3 URL
     */
    public String upload(MultipartFile file, String directory) {
        validateFile(file);

        // UUID를 사용하여 파일명 충돌 방지
        String key = directory + "/" + UUID.randomUUID() + getExtension(file.getOriginalFilename());

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

        } catch (IOException e) {
            log.error("S3 upload failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * S3에서 이미지를 삭제한다.
     *
     * @param imageUrl 삭제할 이미지의 S3 URL
     */
    public void delete(String imageUrl) {
        String key = extractKey(imageUrl);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    // ── private 헬퍼 ─────────────────────────────────────────────────────────────

    /** 파일 타입 및 크기 유효성 검사 */
    private void validateFile(MultipartFile file) {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new CustomException(ErrorCode.FILE_TYPE_INVALID);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    /** 원본 파일명에서 확장자 추출 (예: ".jpg") */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

    /** S3 URL에서 객체 키 추출 (예: "auction-images/uuid.jpg") */
    private String extractKey(String imageUrl) {
        return imageUrl.substring(imageUrl.indexOf(".com/") + 5);
    }
}
