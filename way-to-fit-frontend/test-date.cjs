const dayjs = require('dayjs');
const timezone = require('dayjs/plugin/timezone');
const utc = require('dayjs/plugin/utc');
dayjs.extend(utc);
dayjs.extend(timezone);
dayjs.tz.setDefault('Asia/Seoul');

const baseDate = '2026-04-25';
const startTime = '09:00';
const d = dayjs.tz(`${baseDate}T${startTime}:00`, 'Asia/Seoul');
console.log("toISOString: ", d.toISOString());
console.log("format Z: ", d.format('YYYY-MM-DDTHH:mm:ssZ'));
