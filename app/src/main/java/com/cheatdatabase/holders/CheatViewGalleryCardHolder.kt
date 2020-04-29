package com.cheatdatabase.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.cheatdatabase.R
import com.squareup.picasso.Picasso

class CheatViewGalleryCardHolder(val view: View) : RecyclerView.ViewHolder(view) {

    @BindView(R.id.card_title)
    @JvmField
    internal var title: TextView? = null

    @BindView(R.id.gallery_card_item)
    @JvmField
    internal var card: CardView? = null

    @BindView(R.id.card_image)
    @JvmField
    internal var cardImage: ImageView? = null

    var screenshot: String? = null
        set(screenshot) {
            Picasso.get().load(screenshot).into(cardImage)
        }

    init {
        ButterKnife.bind(this, view)
    }

    override fun toString(): String {
        return super.toString() + " '" + this.screenshot + "'"
    }
}
