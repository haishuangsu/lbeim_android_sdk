package info.hermiths.lbesdk.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
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

    fun isSameDay(current: Date, date: Date): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd")
        return fmt.format(current) == fmt.format(date)
    }

    fun formatHHMMTime(timeStamp: Long): String {
        val formatter = SimpleDateFormat("HH:mm");
        val dateString = formatter.format(Date(timeStamp));
        return dateString
    }

    fun formatYYMMHHMMTime(timeStamp: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm");
        val dateString = formatter.format(Date(timeStamp));
        return dateString
    }
}