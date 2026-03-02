export interface UserResponse {
  id: number;
  email: string;
  nickname: string;
  telegramChatId: string | null;
  role: "USER" | "ADMIN";
  createdAt: string;
}

export interface UserUpdateRequest {
  nickname: string;
  telegramChatId?: string | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}
