package uz.drop.mynotes.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
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
import uz.drop.mynotes.ui.tag.TagActivity
import uz.drop.mynotes.util.SingleBlock
import uz.drop.mynotes.util.extensions.*
import java.util.*

class RecyclerAdapter :
    RecyclerView.Adapter<RecyclerAdapter.VH>(), BaseAdapter {
    private var itemClickListener: SingleBlock<NoteData>? = null
    private var itemCancelClickListener: SingleBlock<NoteData>? = null
    private var itemCompleteClickListener: SingleBlock<NoteData>? = null
    private var itemEditClickListener: SingleBlock<NoteData>? = null
    private var itemDeleteClickListener: SingleBlock<NoteData>? = null
    private var itemClickListener2: ((Int)->Unit)? = null


    private val callback = object : SortedListAdapterCallback<NoteData>(this) {
        override fun areItemsTheSame(item1: NoteData, item2: NoteData): Boolean {
            return item1.id == item2.id
        }

        override fun compare(o1: NoteData, o2: NoteData): Int {
            return if (o1.deadline > o2.deadline) 1 else if (o1.deadline == o2.deadline) 0 else -1

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

    fun setEditClickListener(f: SingleBlock<NoteData>) {
        itemEditClickListener = f
    }

    fun setDeleteClickListener(f: SingleBlock<NoteData>) {
        itemDeleteClickListener = f
    }

    fun setOnClickListener(f: SingleBlock<NoteData>) {
        itemClickListener = f
    }

    fun setOnCancelClickListener(f: SingleBlock<NoteData>) {
        itemCancelClickListener = f
    }

    fun setCompleteClickListener(f: SingleBlock<NoteData>) {
        itemCompleteClickListener = f
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
/*

            itemView.chipGroupScroll.setOnLongClickListener {
                popupMenu(it)
                true
            }
            itemView.chipGroup.setOnLongClickListener {
                popupMenu(it)
                true
            }
*/

            itemView.setOnLongClickListener {
                popupMenu(it)
                true
            }
        }

        private fun popupMenu(it: View) {
            val menuBuilder = MenuBuilder(it.context)
            val menuInflater = MenuInflater(it.context)
            menuInflater.inflate(R.menu.popup_menu, menuBuilder)
            val menu = MenuPopupHelper(it.context, menuBuilder, it)
            menu.setForceShowIcon(true)
            menu.show()
            menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuModeChange(menu: MenuBuilder) = Unit
                override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.edit -> {
                            itemEditClickListener?.invoke(sortedList[adapterPosition])
                        }

                        R.id.delete -> {
                            itemDeleteClickListener?.invoke(sortedList[adapterPosition])
                        }

                        R.id.complete -> {
                            itemCompleteClickListener?.invoke(sortedList[adapterPosition])
                        }

                        R.id.cancel -> {
                            itemCancelClickListener?.invoke(sortedList[adapterPosition])
                        }

                    }
                    return true
                }
            })
        }

        fun bind() = bindItem {
            val (name, date, deadline, hashTags) = sortedList[adapterPosition]
            deadlineText.text = deadline.toDatetime()
            val now = Calendar.getInstance().timeInMillis
            val (days, hours, min) = date.timeDifference(now)
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
            chipGroup.forEach {
                val chip = it as Chip
                val text = chip.text
                chip.setOnClickListener {
                    context.startActivity(Intent(context, TagActivity::class.java).putExtra("tag", text))
                }
            }

            titleNote.text = name
            val timeDifference = now.timeDifference(deadline)
            priorityView.setBackgroundColor(
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

    override fun delete(it: NoteData) {
        sortedList.remove(it)
    }

    override fun add(it: NoteData) {
        sortedList.add(it)
    }

    override fun update(it: NoteData) {
        var index = -1
        for (i in 0 until itemCount) {
            if (sortedList[i].id == it.id) {
                index = i
                break
            }
        }
        if (index != -1)
            sortedList.updateItemAt(index, it)
    }


}
