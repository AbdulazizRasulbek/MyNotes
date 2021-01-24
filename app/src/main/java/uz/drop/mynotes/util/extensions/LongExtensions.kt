package uz.drop.mynotes.util.extensions

import uz.drop.mynotes.data.model.DateDiff
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.time.days

fun Long.toDatetime(pattern: String = "dd MMM yyyy HH:mm"): String? =
    SimpleDateFormat(pattern, Locale.ENGLISH).format(this)

fun Long.timeDifference(afterDate: Long): DateDiff {
    var different = abs(afterDate - this)

    val secondsInMilli: Long = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60
    val daysInMilli = hoursInMilli * 24

    val elapsedDays = (different / daysInMilli).toInt()
    different %= daysInMilli

    val elapsedHours = (different / hoursInMilli).toInt()
    different %= hoursInMilli

    val elapsedMinutes = (different / minutesInMilli).toInt()
    different %= minutesInMilli

    val elapsedSeconds = (different / secondsInMilli).toInt()

    return DateDiff(elapsedDays,elapsedHours,elapsedMinutes,elapsedSeconds)

}