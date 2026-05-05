import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/ko';

// Day.js 플러그인 등록
dayjs.extend(utc);
dayjs.extend(timezone);
dayjs.extend(relativeTime);
dayjs.locale('ko');
dayjs.tz.setDefault('Asia/Seoul');

/**
 * ISO 8601 / Instant 문자열을 한국 시간으로 포맷팅
 */
export function formatDate(date: string | Date, format = 'YYYY.MM.DD') {
  return dayjs(date).tz('Asia/Seoul').format(format);
}

/**
 * 날짜/시간 포맷팅 (시간 포함)
 */
export function formatDateTime(date: string | Date) {
  return dayjs(date).tz('Asia/Seoul').format('YYYY.MM.DD HH:mm');
}

/**
 * 상대 시간 표시 (예: "3분 전")
 */
export function fromNow(date: string | Date) {
  return dayjs(date).fromNow();
}

/**
 * 두 날짜 사이의 차이 (분 단위)
 */
export function diffInMinutes(start: string | Date, end: string | Date) {
  return dayjs(end).diff(dayjs(start), 'minute');
}

export function combineDateAndTimeParts(datePart?: string | null, timePart?: string | null) {
  if (!datePart || !timePart) {
    return undefined;
  }

  const value = dayjs(`${datePart}T${timePart}`);
  return value.isValid() ? value.toDate() : undefined;
}

export function toDateInputValue(value?: string | Date | null) {
  if (!value) {
    return '';
  }

  const parsed = dayjs(value);
  return parsed.isValid() ? parsed.format('YYYY-MM-DD') : '';
}

export function toTimeInputValue(value?: string | Date | null) {
  if (!value) {
    return '';
  }

  const parsed = dayjs(value);
  return parsed.isValid() ? parsed.format('HH:mm') : '';
}

export { dayjs };
