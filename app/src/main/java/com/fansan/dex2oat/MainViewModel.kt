package com.fansan.dex2oat

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.fansan.dex2oat.dao.PackageInfoEntity
import com.fansan.dex2oat.entity.PackageEntity
import com.fansan.dex2oat.entity.SourceType
import com.fansan.dex2oat.entity.TaskEntity
import com.fansan.dex2oat.ui.state.MainScreenStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

/**
 *@author  fansan
 *@version 2023/9/27
 */

class MainViewModel : ViewModel() {

    private val appList = mutableListOf<PackageEntity>()
    var userPackageList = mutableStateListOf<PackageEntity>()
    var userPackageListCompiled = mutableStateListOf<PackageEntity>()
    var systemPackageList = mutableStateListOf<PackageEntity>()
    var systemPackageListCompiled = mutableStateListOf<PackageEntity>()
    val stateHolder = MainScreenStateHolder()

    fun getAllPackageInfo(context: Context) {
        stateHolder.showLoadingPackage()
        val packageManager = context.packageManager
        val list = packageManager.getInstalledPackages(0)
        appList.addAll(list.map {
            PackageEntity(it, false)
        })
        viewModelScope.launch {
            syncDb()
            delay(1.seconds)
            classifyPackage()
            stateHolder.hideLoadingPackage()
        }
    }

    private suspend fun syncDb() {
        withContext(Dispatchers.IO) {
            val allData = Dex2oatDatabase.getDb().packageInfoDao().getAll()
            if (allData.isEmpty()) {
                val list = appList.map {
                    PackageInfoEntity(
                        packageName = it.info.packageName,
                        versionCode = it.info.longVersionCode,
                        lastUpdateTime = it.info.lastUpdateTime,
                        isCompiled = false,
                        modifyTime = System.currentTimeMillis()
                    )
                }
                Dex2oatDatabase.getDb().packageInfoDao().insertAll(list)
            } else {



                val nowAllPackage = appList.map {
                    it.info.packageName
                }

                val dbAllPackage = allData.map {
                    it.packageName
                }

                val newPackage = nowAllPackage.subtract(dbAllPackage.toSet())
                val needDeletePackage = dbAllPackage.subtract(nowAllPackage.toSet())

                if (newPackage.isNotEmpty()) {
                    Dex2oatDatabase.getDb().packageInfoDao().insertAll(appList.filter {
                        newPackage.contains(it.info.packageName)
                    }.map {
                        PackageInfoEntity(
                            packageName = it.info.packageName,
                            versionCode = it.info.longVersionCode,
                            lastUpdateTime = it.info.lastUpdateTime,
                            isCompiled = false,
                            modifyTime = System.currentTimeMillis()
                        )
                    })
                }

                if (needDeletePackage.isNotEmpty()) {
                    Dex2oatDatabase.getDb().packageInfoDao().deleteEntity(allData.filter {
                        needDeletePackage.contains(it.packageName)
                    })
                }

            }
        }
    }

    private fun classifyPackage() {
        val systemApp = appList.filter {
            AppUtils.isAppSystem(it.info.packageName)
        }
        val userApp = appList.subtract(systemApp.toSet())

        systemApp.forEach {
            val packageEntity =
                Dex2oatDatabase.getDb().packageInfoDao().getEntity(it.info.packageName)
            if (packageEntity.isCompiled) {
                if (it.info.lastUpdateTime != packageEntity.lastUpdateTime){
                    systemPackageList.add(it)
                    Dex2oatDatabase.getDb().packageInfoDao().updateEntity(packageEntity.copy(isCompiled = false, modifyTime = System.currentTimeMillis()))
                }
                systemPackageListCompiled.add(it)
            } else {
                systemPackageList.add(it)
            }

        }

        userApp.forEach {
            val packageEntity =
                Dex2oatDatabase.getDb().packageInfoDao().getEntity(it.info.packageName)
            if (packageEntity.isCompiled) {
                if (it.info.lastUpdateTime != packageEntity.lastUpdateTime){
                    userPackageList.add(it)
                    Dex2oatDatabase.getDb().packageInfoDao().updateEntity(packageEntity.copy(isCompiled = false, modifyTime = System.currentTimeMillis()))
                }
                userPackageListCompiled.add(it)
            } else {
                userPackageList.add(it)
            }
        }
    }

    fun selectItem(list: SnapshotStateList<PackageEntity>, index: Int, select: Boolean) {
        list[index] = list[index].copy(isSelected = select)
        updateSelectMode()
    }

    fun scopeSelectAll(type: SourceType){
        when(type){
            SourceType.SP -> {
                val select = systemPackageList.count { it.isSelected } != systemPackageList.size
                val copy = systemPackageList.map {
                    it.copy(isSelected = select)
                }
                systemPackageList.clear()
                systemPackageList.addAll(copy)
            }
            SourceType.SPCompiled -> {
                val select = systemPackageListCompiled.count { it.isSelected } != systemPackageListCompiled.size
                val copy = systemPackageListCompiled.map {
                    it.copy(isSelected = select)
                }
                systemPackageListCompiled.clear()
                systemPackageListCompiled.addAll(copy)
            }
            SourceType.UP -> {
                val select = userPackageList.count { it.isSelected } != userPackageList.size
                val copy = userPackageList.map {
                    it.copy(isSelected = select)
                }
                userPackageList.clear()
                userPackageList.addAll(copy)
            }
            SourceType.UPCompiled -> {
                val select = userPackageListCompiled.count { it.isSelected } != userPackageListCompiled.size
                val copy = userPackageListCompiled.map {
                    it.copy(isSelected = select)
                }
                userPackageListCompiled.clear()
                userPackageListCompiled.addAll(copy)
            }
        }

        updateSelectMode()
    }

    fun cancelAll(){
        val newSystemPackageList = systemPackageList.map {
            it.copy(isSelected = false)
        }
        systemPackageList.clear()
        systemPackageList.addAll(newSystemPackageList)

        val newSystemPackageListCompiled  = systemPackageListCompiled.map {
            it.copy(isSelected = false)
        }
        systemPackageListCompiled.clear()
        systemPackageListCompiled.addAll(newSystemPackageListCompiled)


        val newUserPackageList = userPackageList.map {
            it.copy(isSelected = false)
        }

        userPackageList.clear()
        userPackageList.addAll(newUserPackageList)

        val newUserPackageListCompiled = userPackageListCompiled.map {
            it.copy(isSelected = false)
        }
        userPackageListCompiled.clear()
        userPackageListCompiled.addAll(newUserPackageListCompiled)

        updateSelectMode()
    }

    private fun getAllSelectedPackage():List<TaskEntity>{
        val pckNameList = mutableListOf<TaskEntity>()
        pckNameList.addAll(userPackageList.filter {
            it.isSelected
        }.map {
            TaskEntity(source = SourceType.UP,it.info.packageName)
        })

        pckNameList.addAll(userPackageListCompiled.filter {
            it.isSelected
        }.map {
            TaskEntity(source = SourceType.UPCompiled,it.info.packageName)
        })

        pckNameList.addAll(systemPackageList.filter {
            it.isSelected
        }.map {
            TaskEntity(source = SourceType.SP,it.info.packageName)
        })

        pckNameList.addAll(systemPackageListCompiled.filter {
            it.isSelected
        }.map {
            TaskEntity(source = SourceType.SPCompiled,it.info.packageName)
        })

        return pckNameList
    }

    private fun updateSelectMode() {
        stateHolder.selecteNum =
            userPackageList.count() { it.isSelected } + userPackageListCompiled.count { it.isSelected } + systemPackageList.count {
                it.isSelected
            } + systemPackageListCompiled.count { it.isSelected }
    }

    suspend fun dex2oat(service:IUserService){
        withContext(Dispatchers.IO){
            stateHolder.running = true
            val list = getAllSelectedPackage()
            stateHolder.processCount = list.size
            list.forEachIndexed { index, s ->
                stateHolder.currentProcessIndex = index
                stateHolder.currentProcessName = s.packageName
                val ret = service.dex2oat(s.packageName)
                Log.d("fansangg", "dex2oat: ${ret[0]} -- ${ret[1]} ")
                if (ret[0] == "Success"){
                    val entity = Dex2oatDatabase.getDb().packageInfoDao().getEntity(s.packageName)
                    Dex2oatDatabase.getDb().packageInfoDao().updateEntity(entity.copy(isCompiled = true, modifyTime = System.currentTimeMillis()))
                    handlerSuccess(s)
                }else{
                    handlerFailure(s)
                }
            }
            stateHolder.running = false
        }
    }

    private fun handlerSuccess(taskEntity: TaskEntity){
        when(taskEntity.source){
            SourceType.SP -> {
                val index = systemPackageList.indexOfFirst { taskEntity.packageName == it.info.packageName }
                if (index != -1) {
                    val entity = systemPackageList[index]
                    systemPackageList.removeAt(index)
                    systemPackageListCompiled.add(entity.copy(isSelected = false))
                }
            }
            SourceType.SPCompiled -> {
                val index = systemPackageList.indexOfFirst { taskEntity.packageName == it.info.packageName }
                if (index != -1) {
                    systemPackageListCompiled[index] = systemPackageListCompiled[index].copy(isSelected = false)
                }
            }
            SourceType.UP -> {
                val index = userPackageList.indexOfFirst { taskEntity.packageName == it.info.packageName }
                if (index != -1) {
                    val entity = userPackageList[index]
                    userPackageList.removeAt(index)
                    userPackageListCompiled.add(entity.copy(isSelected = false))
                }
            }
            SourceType.UPCompiled -> {
                val index = userPackageListCompiled.indexOfFirst { taskEntity.packageName == it.info.packageName }
                if (index != -1) {
                    userPackageListCompiled[index] = userPackageListCompiled[index].copy(isSelected = false)
                }
            }
        }

        updateSelectMode()
    }

    private fun handlerFailure(taskEntity: TaskEntity){
        when(taskEntity.source){
            SourceType.SP -> {
                val index = systemPackageList.indexOfFirst { taskEntity.packageName == it.info.packageName }
                if (index != -1) {
                    systemPackageList[index] = systemPackageList[index].copy(isSelected = false)
                }
            }
            SourceType.SPCompiled -> {
                val index = systemPackageList.indexOfFirst { taskEntity.packageName == it.info.packageName }
                if (index != -1) {
                    systemPackageListCompiled[index] = systemPackageListCompiled[index].copy(isSelected = false)
                }
            }
            SourceType.UP -> {
                val index = userPackageList.indexOfFirst { taskEntity.packageName == it.info.packageName }
                if (index != -1) {
                    userPackageList[index] = userPackageList[index].copy(isSelected = false)
                }
            }
            SourceType.UPCompiled -> {
                val index = userPackageListCompiled.indexOfFirst { taskEntity.packageName == it.info.packageName }
                if (index != -1) {
                    userPackageListCompiled[index] = userPackageListCompiled[index].copy(isSelected = false)
                }
            }
        }

        updateSelectMode()
    }
}