package com.cheatdatabase.activity.ui.mycheats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cheatdatabase.data.model.Member

class TopMembersViewModel(private val repositoryUnused: UnpublishedCheatsRepositoryUnused) :
    ViewModel() {

    // Beispiel anhand dieses tutorials: https://www.youtube.com/watch?v=3Uhorh2o1dg&list=PLk7v1Z2rk4hjtIT9TCKIcl2YJYfDlZ_4v&index=3
    // wird aktuell nicht verwendet... (NOCH NICHT FERTIG)

    private val _topMembers = MutableLiveData<List<Member>>()
    val topMembers: LiveData<List<Member>>
        get() = _topMembers

    suspend fun getTopMembers() {
        val topMembers = repositoryUnused.getTopMembers()
        _topMembers.value = topMembers
    }

}