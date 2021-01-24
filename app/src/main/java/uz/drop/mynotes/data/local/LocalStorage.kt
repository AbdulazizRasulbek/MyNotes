package uz.drop.mynotes.data.local

import android.content.Context
import uz.drop.mynotes.util.extensions.BooleanPreference
import uz.drop.mynotes.util.extensions.StringPreference

class LocalStorage private constructor(context: Context) {

    companion object {
        lateinit var instance: LocalStorage
        fun init(context: Context) {
            instance =
                LocalStorage(context)

        }
    }

    private var pref = context.getSharedPreferences("data", Context.MODE_PRIVATE)

    var isFirstTime by BooleanPreference(pref,true)
    var userName by StringPreference(pref,"UserName")
    var email by StringPreference(pref,"example@gmail.com")
    var avatarUrl by StringPreference(pref,"sample")
}