import { getApiErrorMessage } from './api-error';

export function notifySuccess(message: string) {
  window.alert(message);
}

export function notifyError(message: string) {
  window.alert(message);
}

export function notifyApiError(error: unknown, fallbackMessage: string) {
  notifyError(getApiErrorMessage(error, fallbackMessage));
}
