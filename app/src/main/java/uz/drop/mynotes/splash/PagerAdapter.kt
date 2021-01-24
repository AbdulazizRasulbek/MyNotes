package uz.drop.mynotes.splash

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import uz.drop.mynotes.util.extensions.putArguments

class PagerAdapter(private val list: List<Data>, activity: FragmentActivity) :
    FragmentStateAdapter(activity) {
    override fun getItemCount() = list.size

    override fun createFragment(position: Int): Fragment =
        ExampleFragment().putArguments {
            putSerializable("data", list[position])
            putInt("pos", position)

        }


}