package uz.drop.mynotes.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_layout.*
import kotlinx.android.synthetic.main.nav_header_layout.view.*
import uz.drop.mynotes.R
import uz.drop.mynotes.data.local.LocalStorage
import uz.drop.mynotes.data.room.AppDatabase
import uz.drop.mynotes.splash.IntroActivity
import uz.drop.mynotes.ui.RoomFunctions
import uz.drop.mynotes.ui.TermsActivity
import uz.drop.mynotes.ui.all.AllNotesActivity
import uz.drop.mynotes.ui.detail.DetailActivity
import uz.drop.mynotes.ui.dialog.NavHeaderDialog
import uz.drop.mynotes.ui.dialog.NotesDialog
import uz.drop.mynotes.ui.history.HistoryActivity
import uz.drop.mynotes.ui.trash.TrashActivity
import uz.drop.mynotes.util.AppExecutors
import uz.drop.mynotes.util.extensions.loadImage
import java.io.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val adapter = RecyclerAdapter()
    val roomExecutor = AppExecutors().diskIO
    val noteDao = AppDatabase.INSTANCE.noteDao()
    val localStorage = LocalStorage.instance
    override fun onCreate(savedInstanceState: Bundle?) {
        RoomFunctions.checkDeadline()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (localStorage.isFirstTime) {
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }
        setSupportActionBar(toolbar)
        supportActionBar?.title = null
        buttonMenu.setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }
        handleNavigationHeader()
        fabClick()
        initRecycler()
        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun initRecycler() {
        recyclerViewMain.layoutManager = LinearLayoutManager(this)
        recyclerViewMain.adapter = adapter
        adapter.setOnClickListener {
            startActivity(Intent(this, DetailActivity::class.java).putExtra("note", it))
        }

        adapter.apply {
            setEditClickListener {
                Toast.makeText(this@MainActivity, "Edit", Toast.LENGTH_SHORT).show()
                val dialog = NotesDialog(this@MainActivity, "Edit", supportFragmentManager)
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
        }
    }

    private fun fabClick() {
        addFab.setOnClickListener {
            val dialog = NotesDialog(this, "Add", supportFragmentManager)
            dialog.setOnClickListener {
                roomExecutor.execute {
                    val id = noteDao.insert(it)
                    it.id = id
                    runOnUiThread {
                        if (id != 0L)
                            adapter.add(it)
                    }
                }
            }
            dialog.show()
        }
    }

    private fun handleNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        headerView.userNameText.text = localStorage.userName
        headerView.emailText.text = localStorage.email
        headerView.imageAvatar.loadImage(localStorage.avatarUrl)
        headerView.editNavButton.setOnClickListener {
            val dialog = NavHeaderDialog(this, userNameText, emailText, imageAvatar)
            dialog.show()
        }
    }

    override fun onStart() {
        super.onStart()
        /**
         * for waiting for RoomFunctions.checkDealine() finish
         * don't move to onCreate() doesn't work as intended :)
         **/
        submitListToAdapter()
    }

    fun submitListToAdapter() {
        roomExecutor.execute {
            val list = ArrayList(noteDao.getMainNotes())
            runOnUiThread {
                adapter.submitList(list)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mainLayout -> {
                drawer.closeDrawer(GravityCompat.START, true)
            }
            R.id.allNotesLayout -> {
                startActivity(Intent(this, AllNotesActivity::class.java))
                drawer.closeDrawer(GravityCompat.START, true)
            }
            R.id.basketLayout -> {
                startActivity(Intent(this, TrashActivity::class.java))
                drawer.closeDrawer(GravityCompat.START, true)
            }
            R.id.historyLayout -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                drawer.closeDrawer(GravityCompat.START, true)
            }
            R.id.shareNavMenu -> {
                drawer.closeDrawer(GravityCompat.START, true)
                sendApkFile(this)
            }
            R.id.termsLayout -> {
                drawer.closeDrawer(GravityCompat.START, true)
                startActivity(Intent(this, TermsActivity::class.java))
            }
            R.id.aboutLayout -> {
                drawer.closeDrawer(GravityCompat.START, true)
                openPdf()
            }
        }
        return true
    }

    fun openPdf() {
        // Open the PDF file from raw folder
        val inputStream = resources.openRawResource(R.raw.how_to_use)
        val file = File(cacheDir, "mypdf.pdf")
        if (!file.exists()){
            // Copy the file to the cache folder
            inputStream.use { inputStream ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(4 * 1024) // or other buffer size
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
        }

        // Get the URI of the cache file from the FileProvider
        val uri = FileProvider.getUriForFile(this, "$packageName", file)
        if (uri != null) {
            // Create an intent to open the PDF in a third party app
            val pdfViewIntent = Intent(Intent.ACTION_VIEW)
            pdfViewIntent.data = uri
            pdfViewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(pdfViewIntent, "Choos PDF viewer"))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareApplication() {
        val app = applicationContext.applicationInfo
        val filePath = app.sourceDir
        val intent = Intent(Intent.ACTION_SEND)

        // MIME of .apk is "application/vnd.android.package-archive".
        // but Bluetooth does not accept this. Let's use "*/*" instead.
        intent.type = "*/*"

        // Append file and send Intent
        val originalApk = File(filePath)
        try {
            //Make new directory in new location
            var tempFile = File(externalCacheDir.toString() + "/ExtractedApk")
            //If directory doesn't exists create new
            if (!tempFile.isDirectory) if (!tempFile.mkdirs()) return
            //Get application's name and convert to lowercase
            tempFile = File(tempFile.path + "/" + getString(app.labelRes).replace(" ", "").toLowerCase() + ".apk")
            //If file doesn't exists create new
            if (!tempFile.exists()) {
                if (!tempFile.createNewFile()) {
                    return
                }
            }
            //Copy file to new location
            val `in`: InputStream = FileInputStream(originalApk)
            val out: OutputStream = FileOutputStream(tempFile)
            val buf = ByteArray(1024)
            var len: Int = 0
            while (`in`.read(buf).also({ len = it }) > 0) {
                out.write(buf, 0, len)
            }
            `in`.close()
            out.close()
            println("File copied.")
            //Open share dialog
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempFile))
            startActivity(Intent.createChooser(intent, "Share app via"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun sendApkFile(context: Context) {
        try {
            val pm = context.packageManager
            val ai = pm.getApplicationInfo(context.packageName, 0)
            val srcFile = File(ai.publicSourceDir)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "*/*"
            val uri: Uri = FileProvider.getUriForFile(this, context.packageName, srcFile)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            context.grantUriPermission(
                context.packageManager.toString(),
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
