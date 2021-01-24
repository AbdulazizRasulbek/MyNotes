package uz.drop.mynotes.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uz.drop.mynotes.data.room.dao.NoteDao
import uz.drop.mynotes.data.room.entity.NoteData

@Database(entities = [NoteData::class],version = 1)
abstract class AppDatabase:RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        lateinit var INSTANCE: AppDatabase
        fun init(context: Context): AppDatabase {
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

}