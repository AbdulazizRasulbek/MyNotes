package uz.drop.mynotes.ui.all

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
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
import uz.drop.mynotes.data.room.AppDatabase
import uz.drop.mynotes.data.room.entity.NoteData
import uz.drop.mynotes.ui.BaseAdapter
import uz.drop.mynotes.ui.RoomFunctions
import uz.drop.mynotes.ui.detail.DetailActivity
import uz.drop.mynotes.ui.dialog.NotesDialog
import uz.drop.mynotes.ui.main.MainActivity
import uz.drop.mynotes.ui.tag.TagActivity
import uz.drop.mynotes.util.*
import uz.drop.mynotes.util.extensions.*
import java.util.*

class NoteFragment : Fragment(R.layout.note_fragment) {
    lateinit var adapter: RecyclerAdapter
    private val roomExecutor = AppExecutors().diskIO
    private val noteDao = AppDatabase.INSTANCE.noteDao()
    private val mainThread = AppExecutors().mainThread
    var position: Int = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = requireArguments()
        position = bundle.getInt("pos")
        adapter = RecyclerAdapter(position)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        adapter.apply {
            setOnClickListener {
                startActivity(Intent(context, DetailActivity::class.java).putExtra("note", it))
            }
            setEditClickListener {
                val dialog = NotesDialog(context!!, "Edit", fragmentManager!!)
                    .apply { setNoteData(it) }
                dialog.setOnClickListener {
                    RoomFunctions.update(it, adapter)
                }
                dialog.show()
            }
            setDeleteClickListener {
                RoomFunctions.delete(it, adapter)
            }

            setCompleteClickListener {
                RoomFunctions.complete(it, adapter)
            }
            setOnCancelClickListener {
                RoomFunctions.cancel(it, adapter)
            }
            setCloneClickListener {
                val cloneDialog = NotesDialog(context!!, "Clone", fragmentManager!!)
                cloneDialog.setNoteData(it, true)
                cloneDialog.setOnClickListener {
                    roomExecutor.execute {
                        it.rejected = false
                        it.outOfTime = false
                        it.done = false
                        it.deleted = false
                        it.priority = NEW
                        val id = noteDao.insert(it)
                        mainThread.execute {
                            if (id > 0)
                                startActivity(Intent(context, MainActivity::class.java))
                        }
                    }
                }
                cloneDialog.show()
            }

        }

    }

    override fun onResume() {
        super.onResume()
        when (position) {
            NEW -> {
                roomExecutor.execute {
                    val mainNotes = noteDao.getMainNotes()
                    requireActivity().runOnUiThread {
                        adapter.submitList(mainNotes)
                    }
                }
            }
            COMPLETE -> {
                roomExecutor.execute {
                    val list = noteDao.getCompletedNotes()
                    requireActivity().runOnUiThread {
                        adapter.submitList(list)
                    }
                }
            }
            CANCELLED -> {
                roomExecutor.execute {
                    val list = noteDao.getRejectedNotes()
                    requireActivity().runOnUiThread {
                        adapter.submitList(list)
                    }
                }
            }
            OLD -> {
                roomExecutor.execute {
                    val list = noteDao.getOldNotes()
                    requireActivity().runOnUiThread {
                        adapter.submitList(list)
                    }
                }
            }
        }
    }
}

class RecyclerAdapter(private val pos: Int) : RecyclerView.Adapter<RecyclerAdapter.VH>(), BaseAdapter {
    private var itemClickListener: SingleBlock<NoteData>? = null
    private var itemCompleteClickListener: SingleBlock<NoteData>? = null
    private var itemEditClickListener: SingleBlock<NoteData>? = null
    private var itemCancelClickListener: SingleBlock<NoteData>? = null
    private var itemDeleteClickListener: SingleBlock<NoteData>? = null
    private var itemCloneClickListener: SingleBlock<NoteData>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(parent.inflate(R.layout.item_main))

    override fun getItemCount(): Int = sortedList.size()

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(pos)
        if (pos == 0) {
            holder.handleUpcomingTasksMenu()
        } else {
            holder.handleOldTasksMenu()
        }
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

    fun setOnCancelClickListener(f: SingleBlock<NoteData>) {
        itemCancelClickListener = f
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

    fun setCompleteClickListener(f: SingleBlock<NoteData>) {
        itemCompleteClickListener = f
    }

    fun setCloneClickListener(f: SingleBlock<NoteData>) {
        itemCloneClickListener = f
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

        fun handleUpcomingTasksMenu() {
            itemView.setOnLongClickListener {
                val menuBuilder = MenuBuilder(it.context)
                val menuInflater = MenuInflater(it.context)
                menuInflater.inflate(R.menu.popup_menu, menuBuilder)
                val menu = MenuPopupHelper(it.context, menuBuilder, it)
                menu.setForceShowIcon(true)
                menu.show(getScreenResolution(it.context).width / 4, -100)
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
                true
            }
        }

        fun handleOldTasksMenu() {
            itemView.setOnLongClickListener {
                val menuBuilder = MenuBuilder(it.context)
                val menuInflater = MenuInflater(it.context)
                menuInflater.inflate(R.menu.old_popup_menu, menuBuilder)
                val menu = MenuPopupHelper(it.context, menuBuilder, it)
                menu.setForceShowIcon(true)
                menu.show(getScreenResolution(it.context).width / 4, -100)
                menuBuilder.setCallback(object : MenuBuilder.Callback {
                    override fun onMenuModeChange(menu: MenuBuilder) = Unit
                    override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                        when (item.itemId) {
                            R.id.deleteForever -> {
                                itemDeleteClickListener?.invoke(sortedList[adapterPosition])
                            }

                            R.id.clone -> {
                                itemCloneClickListener?.invoke(sortedList[adapterPosition])
                            }
                        }
                        return true
                    }
                })
                true
            }
        }

        fun bind(pos: Int) = bindItem {
            val (name, date, deadline, hashTags, _, _, _, _, _, _, _) = sortedList[adapterPosition]
            deadlineText.text = deadline.toDatetime()
            val now = Calendar.getInstance().timeInMillis
            val (days, hours, min, _) = date.timeDifference(now)
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
            val hashTagsList = hashTags.toTagList()
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
            when (pos) {
                0 -> {
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


    override fun delete(it: NoteData) {
        sortedList.remove(it)
    }

    override fun add(it: NoteData) {
        sortedList.add(it)
    }

    override fun update(it: NoteData) {
        val index = sortedList.indexOf(it)
        sortedList.updateItemAt(index, it)
    }

}
