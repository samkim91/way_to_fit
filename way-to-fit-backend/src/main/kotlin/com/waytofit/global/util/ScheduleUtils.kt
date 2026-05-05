package com.waytofit.global.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object ScheduleUtils {
    val SEOUL_ZONE: ZoneId = ZoneId.of("Asia/Seoul")

    fun todayStartInstant(): Instant =
        LocalDate.now(SEOUL_ZONE).atStartOfDay(SEOUL_ZONE).toInstant()

    fun isToday(instant: Instant): Boolean =
        instant == todayStartInstant()
    
    fun toLocalDate(instant: Instant): LocalDate =
        instant.atZone(SEOUL_ZONE).toLocalDate()

    fun toLocalTime(instant: Instant): LocalTime =
        instant.atZone(SEOUL_ZONE).toLocalTime()
}
