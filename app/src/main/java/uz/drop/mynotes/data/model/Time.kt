package uz.drop.mynotes.data.model

import java.util.*

data class Time(var year: String, var month: String, var day: String, var hour: String, var minute: String, var second: String) {

    fun toPattern(): Time {
        val m = if (month.toInt() < 10) "0${month.toInt() + 1}" else (month + 1)
        val d = if (day.toInt() < 10) "0$day" else day
        val h = if (hour.toInt() < 10) "0$hour" else hour
        val min = if (minute.toInt() < 10) "0$minute" else minute

        return Time(year, m, d, h, min, second)
    }

    override fun toString(): String {
        return "$day.$month.$year $hour:$minute"
    }
}
