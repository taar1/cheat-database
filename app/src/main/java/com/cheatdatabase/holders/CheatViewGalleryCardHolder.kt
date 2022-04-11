package com.cheatdatabase.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.data.model.Screenshot
import com.squareup.picasso.Picasso
import java.io.File

class CheatViewGalleryCardHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var title: TextView? = view.findViewById(R.id.card_title)
    private var cardImage: ImageView? = view.findViewById(R.id.card_image)

    var screenshot: Screenshot? = null
        set(screenshot) {
            Picasso.get().load(screenshot?.fullPath).placeholder(R.drawable.ic_baseline_image)
                .into(cardImage)
            field = screenshot
        }

    var screenshotFile: File? = null
        set(screenshotFile) {
            Picasso.get().load(screenshotFile!!).placeholder(R.drawable.ic_baseline_image)
                .into(cardImage)
            field = screenshotFile
        }

    override fun toString(): String {
        return super.toString() + " '" + this.screenshot + "'"
    }
}
