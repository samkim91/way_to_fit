import axios, { AxiosError } from 'axios';

interface ApiErrorResponse {
  code?: string;
  message?: string;
  data?: unknown;
}

export interface ParsedApiError {
  message: string;
  status?: number;
  code?: string;
  isNetworkError: boolean;
}

function getStatusFallbackMessage(status?: number) {
  if (status === 401) {
    return '로그인이 만료되었습니다. 다시 로그인해주세요.';
  }

  if (status === 403) {
    return '이 작업을 수행할 권한이 없습니다.';
  }

  if (status && status >= 500) {
    return '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
  }

  return null;
}

export function parseApiError(error: unknown, fallbackMessage = '요청 처리 중 오류가 발생했습니다.'): ParsedApiError {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiErrorResponse>;
    const status = axiosError.response?.status;
    const responseMessage = axiosError.response?.data?.message;
    const statusFallbackMessage = getStatusFallbackMessage(status);
    const isNetworkError = !axiosError.response;

    return {
      message: responseMessage || statusFallbackMessage || (isNetworkError ? '네트워크 오류가 발생했습니다. 연결 상태를 확인해주세요.' : fallbackMessage),
      status,
      code: axiosError.response?.data?.code,
      isNetworkError,
    };
  }

  if (error instanceof Error) {
    return {
      message: error.message || fallbackMessage,
      isNetworkError: false,
    };
  }

  return {
    message: fallbackMessage,
    isNetworkError: false,
  };
}

export function getApiErrorMessage(error: unknown, fallbackMessage?: string) {
  return parseApiError(error, fallbackMessage).message;
}
