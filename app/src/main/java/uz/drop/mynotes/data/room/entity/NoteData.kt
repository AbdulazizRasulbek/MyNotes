package uz.drop.mynotes.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "notes"
)
data class NoteData(
    var title: String,
    var date: Long,
    var deadline: Long,
    @ColumnInfo(name = "hash_tags")
    var hashTags: String,
    var priority: Int,
    var description: String,
    var done: Boolean = false,
    var rejected: Boolean = false,
    @ColumnInfo(name = "out_of_time")
    var outOfTime: Boolean = false,
    var deleted: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
) : Serializable

//Priority 0 - new 1 - done 2 -