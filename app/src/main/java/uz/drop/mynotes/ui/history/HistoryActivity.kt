package uz.drop.mynotes.ui.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_history.*
import uz.drop.mynotes.R
import uz.drop.mynotes.data.room.AppDatabase
import uz.drop.mynotes.ui.all.ListData

class HistoryActivity : AppCompatActivity() {
    val list: ArrayList<ListData> = ArrayList()
    private val executor = AppDatabase.INSTANCE.queryExecutor
    private val noteDao = AppDatabase.INSTANCE.noteDao()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(toolbar)
        title = ""
        home.setOnClickListener {
            finish()
        }
        loadList()


    }

    private fun setViewPagerAdapter() {
        val tabTextList = listOf("Done", "Canceled", "Old")
        val tabIcons =
            listOf(
                R.drawable.ic_done_all_black_24dp,
                R.drawable.ic_cancel_black_24dp,
                R.drawable.ic_history_black_24dp
            )
        viewPager2.adapter = PagerAdapterNote(list, this)
        TabLayoutMediator(tabLayout2, viewPager2) { tab, position ->
            tab.setIcon(tabIcons[position])
            tab.text = tabTextList[position]
        }.attach()
    }


    private fun loadList() {
        executor.execute {
            val completedNotes = noteDao.getCompletedNotes()
            val rejectedNotes = noteDao.getRejectedNotes()
            val oldNotes = noteDao.getOldNotes()
            list.add(ListData(completedNotes))
            list.add(ListData(rejectedNotes))
            list.add(ListData(oldNotes))
            runOnUiThread {
                setViewPagerAdapter()
            }
        }

    }
}
