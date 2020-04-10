package com.cheatdatabase.cheat_detail_view

import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.cheatdatabase.R
import com.cheatdatabase.helpers.Konstanten
import com.cheatdatabase.model.Screenshot
import com.squareup.picasso.Picasso

class ImageGalleryCard(screenshot: Screenshot, activity: FragmentActivity) : CardView(activity) {

    private val layout = R.layout.gallery_card

    @BindView(R.id.gallery_card_item)
    @JvmField
    internal var cardView: CardView? = null

    @BindView(R.id.card_title)
    @JvmField
    internal var title: TextView? = null

    @BindView(R.id.card_image)
    @JvmField
    internal var cardImage: ImageView? = null

    init {
        val view = LayoutInflater.from(context).inflate(resources.getLayout(layout), this)
        ButterKnife.bind(this, view)

        Picasso.get().load(Konstanten.SCREENSHOT_ROOT_WEBDIR + "image.php?width=240&image=/cheatpics/" + screenshot.cheatId + screenshot.filename).into(cardImage)
    }

}
