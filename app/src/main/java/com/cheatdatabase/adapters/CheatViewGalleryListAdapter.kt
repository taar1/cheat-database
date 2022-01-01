package com.cheatdatabase.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.callbacks.CheatViewGalleryImageClickListener
import com.cheatdatabase.data.model.Screenshot
import com.cheatdatabase.holders.CheatViewGalleryCardHolder


internal class CheatViewGalleryListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var screenshotList: List<Screenshot> = ArrayList()

    private lateinit var cheatViewGalleryImageClickListener: CheatViewGalleryImageClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.gallery_card, parent, false)
        return CheatViewGalleryCardHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val screenshot: Screenshot = screenshotList[position]

        val cheatViewGalleryCardHolder: CheatViewGalleryCardHolder =
            holder as CheatViewGalleryCardHolder
        holder.view.setOnClickListener {
            cheatViewGalleryImageClickListener.onScreenshotClicked(
                screenshot,
                position
            )
        }

        cheatViewGalleryCardHolder.screenshot = screenshot
        cheatViewGalleryCardHolder.title?.text = (position + 1).toString() // TODO: proper Captions
    }

    fun setClickListener(listener: CheatViewGalleryImageClickListener) {
        this.cheatViewGalleryImageClickListener = listener
    }

    fun setScreenshotList(screenshotList: List<Screenshot>) {
        this.screenshotList = screenshotList
    }

    override fun getItemCount(): Int {
        return screenshotList.size
    }

}