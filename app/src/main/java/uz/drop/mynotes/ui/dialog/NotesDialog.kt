package uz.drop.mynotes.ui.dialog

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.android.synthetic.main.note_dialog.*
import kotlinx.android.synthetic.main.note_dialog.view.*
import uz.drop.mynotes.R
import uz.drop.mynotes.data.model.Time
import uz.drop.mynotes.data.room.entity.NoteData
import uz.drop.mynotes.util.SingleBlock
import uz.drop.mynotes.util.extensions.*
import java.util.*

class NotesDialog(context: Context, actionName: String, fragmentManager: FragmentManager) :
    AlertDialog(context) {
    private val contentView =
        LayoutInflater.from(context).inflate(R.layout.note_dialog, null, false)
    private var listener: SingleBlock<NoteData>? = null
    private var contact: NoteData? = null

    companion object {
        val chipTextColor = Color.parseColor("#4B4C4D")
        var colorList: List<Int> = listOf(
            R.color.material1,
            R.color.material2,
            R.color.material3,
            R.color.material4,
            R.color.material5,
            R.color.material6,
            R.color.material7,
            R.color.material8,
            R.color.material9,
            R.color.material10
        )
    }


    init {
        setTitle("$actionName note")
        setView(contentView)
        contentView.apply {
            loadTagsUi(tagEt, chipGroup, mutableListOf())
            negativeButton.setOnClickListener { dismiss() }
        }
        contentView.dateEt.setOnClickListener {
            val now = Calendar.getInstance()
            val dialog =
                DatePickerDialog.newInstance { _, year, month, dayOfMonth ->
                    TimePickerDialog.newInstance(
                        { _, hourOfDay, minute, second ->
                            val time = Time("$year", "$month", "$dayOfMonth", "$hourOfDay", "$minute", "$second")
                            val date = Calendar.getInstance()
                            date.set(2001,7,11)
                            
                            if (now.checkDate(time)) {
                                if (date.checkTime(time)) {
                                    dateLayout.error = "Select time later than now"
                                } else {
                                    dateLayout.error = null
                                    dateEt.setText(time.toPattern().toString())
                                }
                            } else {
                                dateLayout.error = null
                                dateEt.setText(time.toPattern().toString())
                            }
                        },
                        true
                    ).apply {
                        setAccentColor("#005A81")
                        show(fragmentManager, "")
                    }
                }
            dialog.setAccentColor("#005A81")
            dialog.minDate = now
            dialog.show(fragmentManager, "")

        }
        setPositiveButton(actionName)
    }

    private fun loadTagsUi(
        editText: TextInputEditText,
        chipGroup: ChipGroup,
        currentTags: MutableList<String>
    ) {
        fun addTag(name: String) {
            if (name.isNotEmpty() && !currentTags.contains(name)) {
                addChipToGroup(name, chipGroup, currentTags)
                currentTags.add(name)
            }
        }
        // done keyboard button is pressed
        editText.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val name = textView.text.toString()
                textView.text = null
                addTag(name)
                return@setOnEditorActionListener true
            }
            false
        }

        // space or comma is detected
        editText.addTextChangedListener {
            if (it != null && it.isEmpty()) {
                return@addTextChangedListener
            }

            if (it?.last() == ',' || it?.last() == ' ' || it?.last() == '\n') {
                val name = it.substring(0, it.length - 1)
                addTag(name)
                editText.text = null
            }
        }

    }

    var i = 0
    private fun addChipToGroup(name: String, chipGroup: ChipGroup, items: MutableList<String>) {
        val chip = Chip(context)
        chip.text = name
        chip.chipBackgroundColor = ContextCompat.getColorStateList(context, colorList[i])
        ++i
        chip.isClickable = true
        chip.isCheckable = false
        chip.isCloseIconVisible = true
        chip.setTextColor(Color.DKGRAY)
        chipGroup.addView(chip)
        if (i == 10) i = 0
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip)
            items.remove(name)
        }
    }

    fun setNoteData(data: NoteData, clearDate: Boolean = false) = with(contentView) {
        this@NotesDialog.contact = data
        dateEt.setText(if (clearDate) "" else data.deadline.toDatetime("dd.MM.yyyy hh:mm"))
        titleEt.setText(data.title)
        val list = data.hashTags.toTagList()
        list.forEach {
            addChipToGroup(it, chipGroup, list.toMutableList())
        }
        descriptionEt.setText(data.description)

    }

    fun setOnClickListener(block: SingleBlock<NoteData>) {
        listener = block
    }

    private fun setPositiveButton(text: CharSequence) {
        contentView.positiveButton.text = text
        contentView.positiveButton.setOnClickListener {
            val data = contact ?: NoteData("", 0, 0, "", 0, "")
            val title = contentView.titleEt.text.toString().trim()
            val datetext = contentView.dateEt.text.toString()
            val chipGroup = contentView.chipGroup
            when {
                title.isEmpty() -> {
                    titleLayout.error = "Enter title"
                }
                datetext.isEmpty() -> {
                    titleLayout.error = null
                    dateLayout.error = "Enter date"
                }
                else -> {
                    titleLayout.error = null
                    dateLayout.error = null
                    val deadlineLong = datetext.toLongDate("dd.MM.yyyy hh:mm")
                    data.deadline = deadlineLong
                    data.title = title
                    data.description = contentView.descriptionEt.text.toString().trim()
                    data.date = Calendar.getInstance().timeInMillis
                    val tagList = arrayListOf<String>()
                    chipGroup.forEach {
                        val chip = it as Chip
                        val textChip = chip.text.toString()
                        tagList.add(textChip)
                    }
                    data.hashTags = tagList.toJson()
                    listener?.invoke(data)
                    dismiss()
                }

            }
        }
    }

}