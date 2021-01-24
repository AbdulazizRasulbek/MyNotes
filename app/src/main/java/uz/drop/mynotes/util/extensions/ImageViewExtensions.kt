package uz.drop.mynotes.util.extensions

import android.widget.ImageView
import com.squareup.picasso.Picasso
import uz.drop.mynotes.R


fun ImageView.loadImage(url: String) {
    Picasso.get().load(url).placeholder(R.drawable.avatar).centerCrop()
        .resizeDimen(R.dimen.avatar_image_size, R.dimen.avatar_image_size).into(this)
}