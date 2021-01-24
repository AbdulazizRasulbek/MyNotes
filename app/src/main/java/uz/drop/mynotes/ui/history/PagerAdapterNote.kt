package uz.drop.mynotes.ui.history

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import uz.drop.mynotes.ui.all.ListData
import uz.drop.mynotes.util.extensions.putArguments
import uz.drop.mynotes.util.extensions.toJson

class PagerAdapterNote(private val list: List<ListData>, activity: FragmentActivity) :
    FragmentStateAdapter(activity) {
    override fun getItemCount(): Int =
        list.size

    override fun createFragment(position: Int): Fragment = NoteFragment().putArguments {
        putString("key", list[position].list.toJson())
        putInt("pos",position)
    }
}