package uz.drop.mynotes.ui.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.item_main.view.*
import kotlinx.android.synthetic.main.note_fragment.*
import uz.drop.mynotes.R
import uz.drop.mynotes.data.model.ScreenSize
import uz.drop.mynotes.data.room.entity.NoteData
import uz.drop.mynotes.ui.detail.DetailActivity
import uz.drop.mynotes.ui.dialog.NotesDialog
import uz.drop.mynotes.util.SingleBlock
import uz.drop.mynotes.util.extensions.*
import java.util.*

class NoteFragment : Fragment(R.layout.note_fragment) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = requireArguments()
        val list = bundle.getString("key")!!.toList()
        val pos = bundle.getInt("pos")
        val adapter = HistoryAdapter(pos)
        adapter.submitList(list)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        adapter.setOnClickListener {
            startActivity(Intent(context, DetailActivity::class.java).putExtra("note", it))
        }
    }
}

class HistoryAdapter(private val pos: Int) : RecyclerView.Adapter<HistoryAdapter.VH>() {
    private var itemClickListener: SingleBlock<NoteData>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(parent.inflate(R.layout.item_main))

    override fun getItemCount(): Int = sortedList.size()

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(pos)
    }


    private val callback = object : SortedListAdapterCallback<NoteData>(this) {
        override fun areItemsTheSame(item1: NoteData, item2: NoteData): Boolean {
            return item1.id == item2.id
        }

        override fun compare(o1: NoteData, o2: NoteData): Int {
            return if (o1.deadline > o2.deadline) 1 else if (o1.deadline == o2.deadline) 0 else -1
        }

        override fun areContentsTheSame(oldItem: NoteData, newItem: NoteData): Boolean = oldItem == newItem

    }

    private val sortedList = SortedList(NoteData::class.java, callback)
    private fun replaceAll(list: List<NoteData>) {
        sortedList.beginBatchedUpdates()
        sortedList.replaceAll(list)
        sortedList.endBatchedUpdates()
    }

    fun submitList(list: List<NoteData>) {
        replaceAll(list)
    }

    fun setOnClickListener(f: SingleBlock<NoteData>) {
        itemClickListener = f
    }


    private fun getScreenResolution(context: Context): ScreenSize {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        return ScreenSize(width, height)
    }


    @SuppressLint("RestrictedApi")
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        init {

            itemView.setOnClickListener {
                itemClickListener?.invoke(sortedList[adapterPosition])
            }

        }


        fun bind(position: Int) = bindItem {
            val (name, date, deadline, hashTags) = sortedList[adapterPosition]
            deadlineText.text = deadline.toDatetime()
            val now = Calendar.getInstance().timeInMillis
            val (days, hours, min, sec) = date.timeDifference(now)
            dateText.text = when {
                days > 0 -> {
                    "$days days ago"
                }
                hours > 0 -> {
                    "$hours hours ago"
                }
                min in 3..59 -> {
                    "$min minutes ago"
                }
                else -> {
                    "moments ago"
                }
            }
            titleNote.text = name

            val hashTagsList = hashTags.toTagList()
            hashTagsList.forEach {
                addChipToGroup(it, chipGroup, mutableListOf(), context)
            }
            when (position) {
                0 -> {
                    priorityView.setBackgroundColor(Color.GREEN)
                }
                else -> {
                    priorityView.setBackgroundColor(Color.WHITE)
                }

            }
        }

    }

    var i = 0
    private fun addChipToGroup(name: String, chipGroup: ChipGroup, items: MutableList<String>, context: Context) {
        val chip = Chip(context)
        chip.text = name
        chip.chipBackgroundColor = ContextCompat.getColorStateList(context, NotesDialog.colorList[i])
        ++i
        chip.isClickable = true
        chip.setTextColor(NotesDialog.chipTextColor)
        chip.isCheckable = false
        chipGroup.addView(chip)
        if (i == 10) i = 0
    }

}