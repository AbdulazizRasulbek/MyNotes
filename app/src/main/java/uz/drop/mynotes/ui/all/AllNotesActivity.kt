package uz.drop.mynotes.ui.all

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_all_notes.*
import uz.drop.mynotes.R
import uz.drop.mynotes.data.room.AppDatabase
import uz.drop.mynotes.util.AppExecutors

class AllNotesActivity : AppCompatActivity() {
    var list = ArrayList<ListData>()
    private val executor = AppExecutors().diskIO
    private val dao = AppDatabase.INSTANCE.noteDao()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_notes)
        setSupportActionBar(toolbar)
        title="All Notes"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        loadList()
    }

    private fun setViewPagerAdapter() {
        val tabTextList = listOf("New", "Done", "Canceled", "Old")
        val tabIcons =
            listOf(
                R.drawable.ic_alarm,
                R.drawable.ic_done_all_black_24dp,
                R.drawable.ic_cancel_black_24dp,
                R.drawable.ic_history_black_24dp
            )
        viewPager2.adapter = PagerAdapterNote(list, this)
        TabLayoutMediator(tabLayout2, viewPager2) { tab, position ->
            tab.setIcon(tabIcons[position])
            tab.text=tabTextList[position]
        }.attach()
    }


    private fun loadList() {
        executor.execute {
            val mainNotes = dao.getMainNotes()
            val completedNotes = dao.getCompletedNotes()
            val rejectedNotes = dao.getRejectedNotes()
            val oldNotes = dao.getOldNotes()
            list.add(ListData(mainNotes))
            list.add(ListData(completedNotes))
            list.add(ListData(rejectedNotes))
            list.add(ListData(oldNotes))
            runOnUiThread {
                setViewPagerAdapter()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

}
