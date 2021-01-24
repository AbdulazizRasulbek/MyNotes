package uz.drop.mynotes.util

import android.os.CountDownTimer
import android.widget.TextView
import uz.drop.mynotes.util.extensions.timeDifference
import java.util.*
import java.util.concurrent.TimeUnit


open class MyCount internal constructor(millisInFuture: Long, countDownInterval: Long, private val textView: TextView) :
    CountDownTimer(millisInFuture, countDownInterval) {
    override fun onFinish() {
        textView.text = "Done!"
    }

    override fun onTick(millisUntilFinished: Long) {
        val now = Calendar.getInstance().timeInMillis
        val diff = millisUntilFinished.timeDifference(now)
        val text = "${diff.days} days ${diff.hours}:${diff.min}:${diff.sec}"
        val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
        val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) - (TimeUnit.DAYS.toHours(days))
        val minutes =
            (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)))
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
        val d=when(days){
            0L-> ""
            1L-> "$days day"
            else-> "$days days"
        }
        val h=when(hours){
            0L-> ""
            else-> "$hours:"
        }
        val m=when(minutes){
            in 0..9->"0$minutes:"
            else-> "$minutes:"
        }
        val s=when(seconds){
            in 0..9->"0$seconds"
            else-> "$seconds"
        }

        val hms: String = ("$d $h$m$s")
        textView.text = hms
    }
}