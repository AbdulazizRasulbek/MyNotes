package uz.drop.mynotes.app

import android.app.Application
import android.util.Log
import uz.drop.mynotes.data.local.LocalStorage
import uz.drop.mynotes.data.room.AppDatabase
import uz.drop.mynotes.util.AppExecutors
import java.util.*

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.init(this)
        LocalStorage.init(this)


    }
}