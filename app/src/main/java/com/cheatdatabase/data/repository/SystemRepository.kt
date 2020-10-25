package com.cheatdatabase.data.repository

import androidx.lifecycle.LiveData
import com.cheatdatabase.data.dao.SystemDao
import com.cheatdatabase.data.model.SystemModel

class SystemRepository(private val systemDao: SystemDao) {


    val allSystems: LiveData<List<SystemModel>> = systemDao.all


}