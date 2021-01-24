package uz.drop.mynotes.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.nav_dialog.view.*
import uz.drop.mynotes.R
import uz.drop.mynotes.data.local.LocalStorage
import uz.drop.mynotes.util.extensions.loadImage

class NavHeaderDialog(
    context: Context, userNameView: TextView, emailView: TextView, avatarView: ImageView
) : AlertDialog(context) {
    private val contentView =
        LayoutInflater.from(context).inflate(R.layout.nav_dialog, null, false)
    private val localStorage = LocalStorage.instance

    init {
        setView(contentView)

        contentView.apply {
            usernameEt.setText(localStorage.userName)
            emailEt.setText(localStorage.email)
            imageLinkEt.setText(localStorage.avatarUrl)
        }

        setButton(BUTTON_POSITIVE, "Save") { _, _ ->
            contentView.apply {
                if (usernameEt.text.toString().isEmpty() || emailEt.text.toString()
                        .isEmpty()
                ) {
                    Toast.makeText(
                        context,
                        "Please fill the fields",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                    userNameView.text = usernameEt.text.apply {
                        localStorage.userName = this.toString()
                    }
                    emailView.text = emailEt.text.apply {
                        localStorage.email = this.toString()
                    }
                    if (imageLinkEt.text!!.isNotEmpty())
                        avatarView.loadImage(imageLinkEt.text.toString().apply {
                            localStorage.avatarUrl = this
                        }
                        )


                }
            }
        }
        setButton(BUTTON_NEGATIVE, "Cancel") { _, _ ->

        }
    }


}