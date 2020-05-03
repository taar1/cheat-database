package com.cheatdatabase.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.cheatdatabase.R
import com.cheatdatabase.data.model.Screenshot
import com.squareup.picasso.Picasso
import java.io.File

class CheatViewGalleryCardHolder(val view: View) : RecyclerView.ViewHolder(view) {
    private val TAG = CheatViewGalleryCardHolder::class.java.simpleName


    @BindView(R.id.card_title)
    @JvmField
    internal var title: TextView? = null

    @BindView(R.id.gallery_card_item)
    @JvmField
    internal var card: CardView? = null

    @BindView(R.id.card_image)
    @JvmField
    internal var cardImage: ImageView? = null

    var screenshot: Screenshot? = null
        set(screenshot) {
            Picasso.get().load(screenshot?.fullPath).into(cardImage)
        }

    var screenshotFile: File? = null
        set(screenshotFile) {
            Picasso.get().load(screenshotFile!!).into(cardImage)
        }

    init {
        ButterKnife.bind(this, view)
    }

    override fun toString(): String {
        return super.toString() + " '" + this.screenshot + "'"
    }
}
