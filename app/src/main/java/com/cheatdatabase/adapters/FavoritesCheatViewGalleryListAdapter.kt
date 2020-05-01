package com.cheatdatabase.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.R
import com.cheatdatabase.callbacks.FavoritesCheatViewGalleryImageClickListener
import com.cheatdatabase.holders.CheatViewGalleryCardHolder
import java.io.File
import java.util.*


internal class FavoritesCheatViewGalleryListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var screenshotList: List<File> = ArrayList<File>()

    private lateinit var cheatViewGalleryImageClickListener: FavoritesCheatViewGalleryImageClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.gallery_card, parent, false)
        return CheatViewGalleryCardHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val screenshotFile: File = screenshotList[position]

        val cheatViewGalleryCardHolder: CheatViewGalleryCardHolder = holder as CheatViewGalleryCardHolder
        holder.view.setOnClickListener { cheatViewGalleryImageClickListener.onScreenshotClicked(screenshotFile, position) }

        cheatViewGalleryCardHolder.screenshotFile = screenshotFile
        cheatViewGalleryCardHolder.title?.text = (position + 1).toString() // TODO: proper Captions
    }

    fun setClickListener(listener: FavoritesCheatViewGalleryImageClickListener) {
        this.cheatViewGalleryImageClickListener = listener
    }

    fun setScreenshotUrlList(screenshotUrlList: List<File>) {
        this.screenshotList = screenshotUrlList
    }

    override fun getItemCount(): Int {
        return screenshotList.size
    }

}