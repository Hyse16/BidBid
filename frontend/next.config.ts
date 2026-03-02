import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Docker 이미지 최소화를 위한 standalone 출력 모드
  // 빌드 결과물이 .next/standalone에 자체 실행 가능한 형태로 생성된다
  output: "standalone",

  images: {
    remotePatterns: [
      {
        protocol: "https",
        hostname: "*.amazonaws.com",
        pathname: "/**",
      },
    ],
  },
};

export default nextConfig;
