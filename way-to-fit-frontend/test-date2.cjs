const dayjs = require('dayjs');
const utc = require('dayjs/plugin/utc');
dayjs.extend(utc);

const baseDate = '2026-04-25';
const startTime = '09:00';

// If we want to send "2026-04-25T09:00:00.000Z" (pretending local is UTC)
console.log(dayjs.utc(`${baseDate}T${startTime}:00`).toISOString());
