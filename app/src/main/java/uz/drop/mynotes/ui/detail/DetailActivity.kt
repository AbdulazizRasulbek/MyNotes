package uz.drop.mynotes.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.activity_detail.*
import uz.drop.mynotes.R
import uz.drop.mynotes.data.room.entity.NoteData
import uz.drop.mynotes.ui.dialog.NotesDialog
import uz.drop.mynotes.ui.dialog.NotesDialog.Companion.colorList
import uz.drop.mynotes.ui.tag.TagActivity
import uz.drop.mynotes.util.MyCount
import uz.drop.mynotes.util.extensions.toDatetime
import uz.drop.mynotes.util.extensions.toTagList
import java.util.*

class DetailActivity : AppCompatActivity() {
    private lateinit var timer: MyCount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        title = "Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val note = intent.getSerializableExtra("note") as NoteData
        titleText.text = note.title
        date.text = note.deadline.toDatetime()
        if (note.description.isNotEmpty())
            descriptionText.text = note.description
        else descriptionGroup.visibility = View.GONE
        timer = object : MyCount(note.deadline - Calendar.getInstance().timeInMillis, 1000, remainingDate) {}
        val hashTags = note.hashTags.toTagList()
        if (hashTags.isEmpty()) {
            tagGroup.visibility = View.GONE
        }
        hashTags.forEach {
            addChipToGroup(it, chipGroup, mutableListOf())
        }
        chipGroup.forEach {
            val text = (it as Chip).text.toString()
            it.setOnClickListener {
                startActivity(Intent(this, TagActivity::class.java).putExtra("tag", text))
            }
        }
        note.apply {
            when {
                rejected -> {
                    remainingDate.text = "Canceled!"
                }
                outOfTime -> {
                    remainingDate.text = "Old!"
                }
                deleted -> {
                    remainingDate.text = "Deleted!"
                }
                done -> {
                    remainingDate.text = "Done!"
                }
                else -> timer.start()
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    var i = 0
    private fun addChipToGroup(name: String, chipGroup: ChipGroup, items: MutableList<String>) {
        val chip = Chip(this)
        chip.text = name
        chip.chipBackgroundColor = ContextCompat.getColorStateList(this, colorList[i])
        ++i
        chip.isClickable = true
        chip.setTextColor(NotesDialog.chipTextColor)
        chip.isCheckable = false
        chipGroup.addView(chip)
        if (i == 10) i = 0
    }
}
