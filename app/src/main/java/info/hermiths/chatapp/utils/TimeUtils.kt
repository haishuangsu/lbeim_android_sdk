package info.hermiths.chatapp.utils

import java.time.Instant
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date

object TimeUtils {
    fun timeStampGen(): Long {
        val inst = Instant.now()
        val insOffsetTime = inst.atOffset(ZoneOffset.UTC)
        val utcStamp = insOffsetTime.toEpochSecond()
        val insOffsetUtc = insOffsetTime.offset

        val timeStamp = System.currentTimeMillis()
        println("Time --->> insOffsetTime: $insOffsetTime, insUtcOffset: $insOffsetUtc")
        println("Time --->> timeStamp: $timeStamp, utcStamp: $utcStamp")
        return timeStamp
    }

    fun formatTime(timeStamp: Long): String {
        return ""
    }

    private const val PER_MIN = 60 * 1000L
    private const val PER_HOUR = 60 * PER_MIN
    private const val PER_DAY = 24 * PER_HOUR
    private const val PER_WEEK = 7 * PER_DAY
    var sCurrent: Date? = null
    fun getRelativeDesc(timestamp: Long): String {
        val inputDate = Date(timestamp)
        val cur: Date = sCurrent ?: Date()
        val delta: Long = cur.time - inputDate.time
        if (delta <= 5 * PER_MIN) {
            return "刚刚"
        }
        if (delta < PER_HOUR) {
            return (delta / PER_MIN).toString() + "分钟前"
        }
        if (delta < PER_DAY) {
            return (delta / PER_HOUR).toString() + "小时前"
        }
        if (delta < 2 * PER_DAY) {
            return "昨天"
        }
        if (delta < 7 * PER_DAY) {
            return (delta / PER_DAY).toString() + "天前"
        }
        if (delta < 4 * PER_WEEK) {
            return (delta / PER_WEEK).toString() + "周前"
        }
        val calendar = Calendar.getInstance()
        calendar.time = cur
        calendar.add(Calendar.MONTH, -1)
        var compareDelta: Long = cur.time - calendar.time.time
        if (delta < compareDelta) {
            return "4周前"
        }
        for (i in 1..11) {
            calendar.add(Calendar.MONTH, -1)
            compareDelta = cur.time - calendar.time.time
            if (delta < compareDelta) {
                return i.toString() + "月前"
            }
        }
        calendar.time = inputDate
        return calendar[Calendar.YEAR].toString() + '-' + String.format(
            "%02d", calendar[Calendar.MONTH] + 1
        ) + '-' + String.format("%02d", calendar[Calendar.DATE])
    }
}