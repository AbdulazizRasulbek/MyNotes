package uz.drop.mynotes.util.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment

fun Fragment.putArguments(block: Bundle.() -> Unit): Fragment {
    val bundle = arguments ?: Bundle()
    block(bundle)
    arguments = bundle
    return this
}