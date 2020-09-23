package com.cheatdatabase.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.data.model.Screenshot
import com.squareup.picasso.Picasso
import java.io.File

class CheatViewGalleryCardHolder(val view: View) : RecyclerView.ViewHolder(view) {
    private val TAG = CheatViewGalleryCardHolder::class.java.simpleName

    internal var title: TextView? = view.findViewById(R.id.card_title)

    internal var card: CardView? = view.findViewById(R.id.gallery_card_item)

    private var cardImage: ImageView? = view.findViewById(R.id.card_image)

    var screenshot: Screenshot? = null
        set(screenshot) {
            Picasso.get().load(screenshot?.fullPath).into(cardImage)
        }

    var screenshotFile: File? = null
        set(screenshotFile) {
            Picasso.get().load(screenshotFile!!).into(cardImage)
        }

    override fun toString(): String {
        return super.toString() + " '" + this.screenshot + "'"
    }
}
