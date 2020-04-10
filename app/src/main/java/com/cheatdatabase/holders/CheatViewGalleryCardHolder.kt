package com.cheatdatabase.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.cheatdatabase.R
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.model.Screenshot
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

    var screenshot: Screenshot? = null
        set(screenshot) {
            field = screenshot

            title?.text = screenshot?.filename?.removeSuffix(".png")?.toUpperCase()

            val sceenshotPath: String = Konstanten.SCREENSHOT_ROOT_WEBDIR + "image.php?width=200&image=/cheatpics/" + screenshot?.cheatId + screenshot?.filename
            Picasso.get().load(sceenshotPath).into(cardImage)
        }

    init {
        ButterKnife.bind(this, view)
    }

    override fun toString(): String {
        return super.toString() + " '" + this.screenshot!!.filename + "'"
    }
}
