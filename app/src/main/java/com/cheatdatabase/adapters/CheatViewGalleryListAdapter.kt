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
    private var homepagePosterListElementList: List<Screenshot> = ArrayList<Screenshot>()
    private var screenshotUrlList: List<String> = ArrayList<String>()

    private lateinit var cheatViewGalleryImageClickListener: CheatViewGalleryImageClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.gallery_card, parent, false)
        return CheatViewGalleryCardHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val screenshotUrl: String = screenshotUrlList[position]
        val screenshot: Screenshot = homepagePosterListElementList[position]

        val cheatViewGalleryCardHolder: CheatViewGalleryCardHolder = holder as CheatViewGalleryCardHolder
        holder.view.setOnClickListener { cheatViewGalleryImageClickListener.onScreenshotClicked(screenshot, position) }

        cheatViewGalleryCardHolder.screenshot = screenshot
        cheatViewGalleryCardHolder.title?.text = (position + 1).toString() // TODO: proper Captions
    }

    fun setClickListener(listener: CheatViewGalleryImageClickListener) {
        this.cheatViewGalleryImageClickListener = listener
    }

    fun setScreenshotList(screenshotList: List<Screenshot>) {
        this.homepagePosterListElementList = screenshotList
    }

    fun setScreenshotUrlList(screenshotUrlList: List<String>) {
        this.screenshotUrlList = screenshotUrlList
    }

    override fun getItemCount(): Int {
        return homepagePosterListElementList.size
    }

}