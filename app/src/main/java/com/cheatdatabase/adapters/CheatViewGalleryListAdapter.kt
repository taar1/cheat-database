package com.cheatdatabase.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.callbacks.CheatViewGalleryImageClickListener
import com.cheatdatabase.holders.CheatViewGalleryCardHolder
import com.cheatdatabase.model.Screenshot
import java.util.*


internal class CheatViewGalleryListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var homepagePosterListElementList: List<Screenshot>
    private lateinit var cheatViewGalleryImageClickListener: CheatViewGalleryImageClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.gallery_card, parent, false)
        return CheatViewGalleryCardHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val screenshot: Screenshot = homepagePosterListElementList[position]
        val cheatViewGalleryCardHolder: CheatViewGalleryCardHolder = holder as CheatViewGalleryCardHolder
        holder.view.setOnClickListener { v -> cheatViewGalleryImageClickListener.onScreenshotClicked(screenshot, position) }

        cheatViewGalleryCardHolder.screenshot = screenshot
    }

    fun setClickListener(listener: CheatViewGalleryImageClickListener) {
        this.cheatViewGalleryImageClickListener = listener
    }

    fun setScreenshotList(screenshotList: List<Screenshot>) {
        this.homepagePosterListElementList = screenshotList
    }

    override fun getItemCount(): Int {
        return homepagePosterListElementList.size
    }

    init {
        homepagePosterListElementList = ArrayList<Screenshot>()
    }
}