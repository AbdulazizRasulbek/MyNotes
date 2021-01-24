package uz.drop.mynotes.ui.trash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_archive.*
import uz.drop.mynotes.R
import uz.drop.mynotes.data.room.AppDatabase
import uz.drop.mynotes.ui.detail.DetailActivity
import uz.drop.mynotes.util.AppExecutors

class TrashActivity : AppCompatActivity() {
    private val roomExecutor = AppExecutors().diskIO
    private val noteDao = AppDatabase.INSTANCE.noteDao()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive)
        trashRecycler.layoutManager = LinearLayoutManager(this)
        val adapter = RecyclerTrash()
        trashRecycler.adapter = adapter
        roomExecutor.execute {
            val list = noteDao.getDeletedNotes()
            runOnUiThread {
                adapter.submitList(list)
            }
        }
        home.setOnClickListener {
            finish()
        }

        adapter.setOnClickListener {
            startActivity(Intent(this, DetailActivity::class.java).putExtra("note",it))
        }

        adapter.setDeleteClickListener {
            AlertDialog.Builder(this).apply {
                setMessage("Item deleted forever. Are you sure?")
                setTitle("Delete")
                setPositiveButton("Yes") { _, _ ->
                    roomExecutor.execute {
                        val delete = noteDao.delete(it)
                        runOnUiThread {
                            if (delete > 0)
                                adapter.delete(it)
                        }
                    }
                }
                setNegativeButton("Cancel", null)
                setCancelable(false)
                show()
            }
        }
        adapter.setRestoreClickListener {
            roomExecutor.execute {
                it.deleted = false
                val update = noteDao.update(it)
                runOnUiThread {
                    if(update>0)
                        adapter.delete(it)
                    Snackbar.make(findViewById(android.R.id.content),"Item restored",Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }
}
