package uz.drop.mynotes.ui

import uz.drop.mynotes.data.room.AppDatabase
import uz.drop.mynotes.data.room.entity.NoteData
import uz.drop.mynotes.ui.all.RecyclerAdapter
import uz.drop.mynotes.util.*
import java.util.*

class RoomFunctions {

    companion object {
         val roomExecutor = AppExecutors().diskIO
         val noteDao = AppDatabase.INSTANCE.noteDao()
         val mainThread = AppExecutors().mainThread
       fun update(it: NoteData, adapter: BaseAdapter) {
            roomExecutor.execute {
                val update = noteDao.update(it)
                mainThread.execute {
                    if (update > 0)
                        adapter.update(it)
                }
            }
        }

        fun delete(it: NoteData, adapter: BaseAdapter) {
            roomExecutor.execute {
                it.deleted = true
                it.priority = DELETED
                val update = noteDao.update(it)
                mainThread.execute {
                    if (update > 0)
                        adapter.delete(it)
                }
            }
        }

        fun complete(it: NoteData, adapter: BaseAdapter) {
            roomExecutor.execute {
                it.done = true
                it.priority = COMPLETE
                val update = noteDao.update(it)
                mainThread.execute {
                    if (update > 0)
                        adapter.delete(it)
                }
            }
        }

        fun cancel(it: NoteData, adapter: BaseAdapter) {
            roomExecutor.execute {
                it.rejected = true
                it.priority = CANCELLED
                val update = noteDao.update(it)
                mainThread.execute {
                    if (update > 0)
                        adapter.delete(it)
                }
            }
        }

        /**
         * Function for comparing items' deadline to now and set the old ones' "outOfTime" field true
         */
        fun checkDeadline() {
            roomExecutor.execute {
                noteDao.getMainNotes().forEach {
                    if (it.deadline - System.currentTimeMillis() <= 0) {
                        it.outOfTime = true
                        it.priority = OLD
                        noteDao.update(it)
                    }
                }
            }
        }
    }
}