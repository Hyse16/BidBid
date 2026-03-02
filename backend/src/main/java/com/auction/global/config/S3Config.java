package com.auction.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 클라이언트 설정
 *
 * 경매 상품 이미지를 S3에 저장하기 위한 클라이언트를 빈으로 등록한다.
 * 인증 정보(accessKey, secretKey)는 application.yml의 환경변수로 주입받아
 * 코드에 하드코딩되지 않도록 관리한다.
 *
 * 리전은 ap-northeast-2 (서울)을 기본값으로 사용한다.
 */
@Configuration
public class S3Config {

    @Value("${aws.credentials.access-key}")
    private String accessKey; // IAM 사용자 Access Key (환경변수로 주입)

    @Value("${aws.credentials.secret-key}")
    private String secretKey; // IAM 사용자 Secret Key (환경변수로 주입)

    @Value("${aws.region}")
    private String region;    // S3 버킷이 위치한 리전 (예: ap-northeast-2)

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
