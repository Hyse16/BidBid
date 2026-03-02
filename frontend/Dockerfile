# ──────────────────────────────────────────────────────────────────────────────
# Stage 1: Dependencies (npm 캐시 레이어 분리)
# ──────────────────────────────────────────────────────────────────────────────
FROM node:20-alpine AS deps

WORKDIR /app

# package.json만 먼저 복사 → node_modules 캐시 활용
COPY package.json package-lock.json* ./
RUN npm ci --only=production

# ──────────────────────────────────────────────────────────────────────────────
# Stage 2: Builder (Next.js 빌드)
# ──────────────────────────────────────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci

# 소스 복사 후 Next.js 빌드
COPY . .

# 빌드 시점 환경 변수 (실제 값은 docker-compose 또는 배포 환경에서 주입)
ARG NEXT_PUBLIC_API_URL=http://localhost:8080
ARG NEXT_PUBLIC_WS_URL=ws://localhost:8080
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_WS_URL=$NEXT_PUBLIC_WS_URL

RUN npm run build

# ──────────────────────────────────────────────────────────────────────────────
# Stage 3: Runtime (최소 이미지)
# ──────────────────────────────────────────────────────────────────────────────
FROM node:20-alpine AS runtime

WORKDIR /app

# 보안: 전용 유저로 실행
RUN addgroup -S nextjs && adduser -S nextjs -G nextjs

# Next.js standalone 출력물만 복사 (이미지 크기 최소화)
# next.config.ts에 output: 'standalone' 옵션이 필요하다
COPY --from=builder --chown=nextjs:nextjs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nextjs /app/.next/static ./.next/static
COPY --from=builder --chown=nextjs:nextjs /app/public ./public

USER nextjs

# 포트 노출
EXPOSE 3000

ENV PORT=3000
ENV NODE_ENV=production
ENV HOSTNAME="0.0.0.0"

# Next.js standalone 서버 실행
CMD ["node", "server.js"]
