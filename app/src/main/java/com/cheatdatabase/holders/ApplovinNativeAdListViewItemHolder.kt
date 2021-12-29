package com.cheatdatabase.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.cheatdatabase.databinding.IncludeApplovinMaxadviewNativeBinding

class ApplovinNativeAdListViewItemHolder(val binding: IncludeApplovinMaxadviewNativeBinding) :
    RecyclerView.ViewHolder(binding.root.rootView) {

    fun showIt() {
        with(binding.maxAdView) {
            visibility = View.VISIBLE
            alpha = 0.5f
            startAutoRefresh()
            loadAd()
        }
    }
}

