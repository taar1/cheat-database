package com.cheatdatabase.activity.ui.myunpublishedcheatslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.cheatdatabase.R
import com.cheatdatabase.data.network.MyUnpublishedCheatsNetworkDataSourceImpl
import com.cheatdatabase.rest.KotlinRestApi
import kotlinx.android.synthetic.main.my_unpublished_cheats_list_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyUnpublishedCheatsListFragment : Fragment() {

    private lateinit var viewModel: MyUnpublishedCheatsListViewModel

    companion object {
        fun newInstance() = MyUnpublishedCheatsListFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.my_unpublished_cheats_list_fragment, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MyUnpublishedCheatsListViewModel::class.java)
        // TODO: Use the ViewModel

        val apiService = KotlinRestApi()
        val myUnpublishedCheatsNetworkDataSource = MyUnpublishedCheatsNetworkDataSourceImpl(apiService)

        myUnpublishedCheatsNetworkDataSource.downloadCheatList.observe(this, Observer {
            testText.text = it.toString()
        })

        // TODO hier weitermachen: https://www.youtube.com/watch?v=DwnloROxaKg
        // bzw. DI von kapitel 05 evtl. noch nachholen....

        GlobalScope.launch(Dispatchers.Main) {
//            val response = apiService.getCheatsByMemberId(11).await()
//            testText.text = "asldkfhasd"

            myUnpublishedCheatsNetworkDataSource.fetchCheatList(11)
        }
    }

}
