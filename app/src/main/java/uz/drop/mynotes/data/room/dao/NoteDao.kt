package uz.drop.mynotes.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import uz.drop.mynotes.data.room.entity.NoteData

@Dao
interface NoteDao : BaseDao<NoteData> {

    @Query("Select * from notes")
    fun getAll(): List<NoteData>

    @Query("Select * from notes where rejected = 1")
    fun getRejectedNotes(): List<NoteData>

    @Query("Select * from notes where deleted = 1")
    fun getDeletedNotes(): List<NoteData>

    @Query("Select * from notes where out_of_time = 1")
    fun getOldNotes(): List<NoteData>

    @Query("Select * from notes where done = 1")
    fun getCompletedNotes(): List<NoteData>

    @Query("Select * from notes where done = 0 and rejected = 0 and out_of_time = 0 and deleted=0")
    fun getMainNotes(): List<NoteData>

    @Query("Select * from notes where hash_tags like '%' ||:text|| '%' order by priority")
    fun getNotesByTag(text: String?): List<NoteData>

}