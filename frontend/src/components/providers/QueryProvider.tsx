"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState } from "react";

/**
 * TanStack Query 클라이언트 Provider
 *
 * "use client" 컴포넌트에서 QueryClient를 생성하여
 * 서버 컴포넌트인 RootLayout에서도 사용할 수 있도록 분리한다.
 */
export default function QueryProvider({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 30 * 1000, // 기본 캐시 유효 시간: 30초
            retry: 1,
          },
        },
      })
  );

  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}
