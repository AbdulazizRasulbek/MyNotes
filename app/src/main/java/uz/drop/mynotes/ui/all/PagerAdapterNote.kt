package uz.drop.mynotes.ui.all

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import uz.drop.mynotes.util.extensions.putArguments

class PagerAdapterNote(private val list: List<ListData>, activity: FragmentActivity) :
    FragmentStateAdapter(activity) {
    override fun getItemCount(): Int =
        list.size

    override fun createFragment(position: Int): Fragment = NoteFragment().putArguments {
        putInt("pos", position)
    }
}