# ──────────────────────────────────────────────────────────────────────────────
# Stage 1: Build (Gradle + Java 17)
# 의존성 레이어를 분리하여 소스 변경 시 캐시를 최대한 재사용한다.
# ──────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# 의존성 캐시 레이어: wrapper + build script만 먼저 복사
COPY gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q

# 소스 복사 후 JAR 빌드 (테스트 제외 — CI에서 별도 실행)
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test -q

# ──────────────────────────────────────────────────────────────────────────────
# Stage 2: Runtime (JRE만 포함 → 이미지 크기 최소화)
# ──────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# 보안: 루트 계정으로 실행하지 않도록 전용 유저 생성
RUN addgroup -S auction && adduser -S auction -G auction

# 빌드 산출물 복사 및 소유자 설정
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown auction:auction app.jar

USER auction

# 포트 노출
EXPOSE 8080

# JVM 옵션: 컨테이너 메모리 제한 자동 인식, 보안 랜덤 소스 설정
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
