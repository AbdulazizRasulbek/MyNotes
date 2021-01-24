package uz.drop.mynotes.ui.tag

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_tag.*
import uz.drop.mynotes.R
import uz.drop.mynotes.data.room.AppDatabase
import uz.drop.mynotes.util.AppExecutors

class TagActivity : AppCompatActivity() {

    val adapter = TagAdapter()
    private val executor = AppExecutors().diskIO
    private val noteDao = AppDatabase.INSTANCE.noteDao()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag)
        val tag = intent.getStringExtra("tag")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        executor.execute {
            val notesByTag = noteDao.getNotesByTag(tag)
            runOnUiThread {
                recyclerViewTag.layoutManager = LinearLayoutManager(this)
                recyclerViewTag.adapter = adapter
                adapter.submitList(notesByTag)
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

}
