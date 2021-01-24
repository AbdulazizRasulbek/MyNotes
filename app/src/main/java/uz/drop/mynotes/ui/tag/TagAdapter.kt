package uz.drop.mynotes.ui.tag

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.item_main.view.*
import uz.drop.mynotes.R
import uz.drop.mynotes.data.model.ScreenSize
import uz.drop.mynotes.data.room.entity.NoteData
import uz.drop.mynotes.ui.BaseAdapter
import uz.drop.mynotes.ui.dialog.NotesDialog
import uz.drop.mynotes.ui.dialog.NotesDialog.Companion.chipTextColor
import uz.drop.mynotes.util.SingleBlock
import uz.drop.mynotes.util.extensions.*
import java.util.*

class TagAdapter :
    RecyclerView.Adapter<TagAdapter.VH>(){
    private var itemClickListener: SingleBlock<NoteData>? = null
    private var itemCancelClickListener: SingleBlock<NoteData>? = null
    private var itemCompleteClickListener: SingleBlock<NoteData>? = null
    private var itemEditClickListener: SingleBlock<NoteData>? = null
    private var itemDeleteClickListener: SingleBlock<NoteData>? = null


    private val callback = object : SortedListAdapterCallback<NoteData>(this) {
        override fun areItemsTheSame(item1: NoteData, item2: NoteData): Boolean {
            return item1.id == item2.id
        }

        override fun compare(o1: NoteData, o2: NoteData): Int {
            return when {
                o1.priority > o2.priority -> 1
                o1.priority == o2.priority -> o1.deadline.compareTo(o2.deadline)
                else -> -1
            }
        }

        override fun areContentsTheSame(oldItem: NoteData, newItem: NoteData): Boolean =
            oldItem.title == newItem.title && oldItem.deadline == newItem.deadline && oldItem.date == newItem.date && oldItem.hashTags == newItem.hashTags
                    && oldItem.deleted == newItem.deleted && oldItem.done == newItem.done && oldItem.outOfTime == newItem.outOfTime
                    && oldItem.rejected == newItem.rejected

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


    @SuppressLint("RestrictedApi")
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener {
                itemClickListener?.invoke(sortedList[adapterPosition])
            }
        }

        fun bind() = bindItem {
            val (title, date, deadline, hashTags,
                priority, description,
                done, rejected, outOfTime, deleted) = sortedList[adapterPosition]
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
                min > 2 -> {
                    "$min minutes ago"
                }
                else -> {
                    "moments ago"
                }
            }
            val hashTagsList = hashTags.toTagList()
            if (chipGroup.size > 0) chipGroup.removeAllViews()
            hashTagsList.forEach {
                addChipToGroup(it, chipGroup, mutableListOf(), context)
            }

            titleNote.text = title
            val timeDifference = now.timeDifference(deadline)
            priorityView.setBackgroundColor(
                when {
                    rejected || done || outOfTime || deleted -> Color.LTGRAY
                    else -> {
                        when (timeDifference.days) {
                            0 -> {
                                Color.RED
                            }
                            in 1..3 -> {
                                Color.YELLOW
                            }
                            else -> {
                                Color.BLUE
                            }

                        }
                    }
                }

            )

        }

        private var i = 0
        private fun addChipToGroup(name: String, chipGroup: ChipGroup, items: MutableList<String>, context: Context) {
            val chip = Chip(context)
            chip.text = name
            chip.chipBackgroundColor = ContextCompat.getColorStateList(context, NotesDialog.colorList[i++])
            chip.isClickable = true
            chip.setTextColor(chipTextColor)
            chip.isCheckable = false
            chipGroup.addView(chip)
            if (i == 10) i = 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(parent.inflate(R.layout.item_main))

    override fun getItemCount(): Int = sortedList.size()

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind()


}
