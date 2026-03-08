// 로그인 페이지는 헤더/푸터 없이 풀스크린으로 렌더링
export default function LoginLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}
