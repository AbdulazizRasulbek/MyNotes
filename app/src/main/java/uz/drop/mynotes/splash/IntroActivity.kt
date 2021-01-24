package uz.drop.mynotes.splash

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.*
import uz.drop.mynotes.R
import uz.drop.mynotes.data.local.LocalStorage
import uz.drop.mynotes.ui.main.MainActivity

class IntroActivity : AppIntro2(){
    private val data = ArrayList<Data>()
    private val local = LocalStorage.instance
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addSlide(
            AppIntroFragment.newInstance(
                "Write notes",
                "Add, remove and edit your notes as you go",
                R.drawable.ic_note,
                Color.parseColor("#fffafdf6"),
                titleColor=Color.parseColor("#013454"),descriptionColor = Color.DKGRAY
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                "Smart search",
                "Search your notes with tags easily",
                R.drawable.ic_label,
                Color.parseColor("#fffafdf6"),
                titleColor=Color.parseColor("#013454"),descriptionColor = Color.DKGRAY
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                "Real-time availibilty",
                "Cancel tasks, mark as completed, clone old ones",
                R.drawable.ic_calendar1,
                Color.parseColor("#fffafdf6"),
                titleColor=Color.parseColor("#013454"),descriptionColor = Color.DKGRAY
            )
        )

        setTransformer(AppIntroPageTransformerType.Depth)
        setIndicatorColor(Color.parseColor("#005A81"),Color.DKGRAY)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        navigate()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        navigate()
    }
    private fun navigate(){
        startActivity(Intent(this, MainActivity::class.java))
        local.isFirstTime = false
        finish()
    }
}
