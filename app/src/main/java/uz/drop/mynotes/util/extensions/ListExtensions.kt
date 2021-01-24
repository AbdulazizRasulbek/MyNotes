package uz.drop.mynotes.util.extensions

import com.google.gson.Gson

fun <T> List<T>.toJson(): String {
    return Gson().toJson(this)
}