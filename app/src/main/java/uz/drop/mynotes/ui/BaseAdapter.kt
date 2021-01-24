package uz.drop.mynotes.ui

import uz.drop.mynotes.data.room.entity.NoteData

interface BaseAdapter {
    fun update(it:NoteData)
    fun delete(it: NoteData)
    fun add(it: NoteData)
}