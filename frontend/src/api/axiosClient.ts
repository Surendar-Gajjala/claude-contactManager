import axios, { AxiosError } from 'axios';
import type { ApiErrorResponse } from './types';

const axiosClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiErrorResponse>;
    const data = axiosError.response?.data;
    if (data?.details?.length) {
      return `${data.message}: ${data.details.join('; ')}`;
    }
    if (data?.message) {
      return data.message;
    }
    if (axiosError.message) {
      return axiosError.message;
    }
  }
  return 'Something went wrong. Please try again.';
}

export default axiosClient;
