package com.fansan.dex2oat

import android.content.Context
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansan.dex2oat.dao.PackageInfoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *@author  fansan
 *@version 2023/9/27
 */

class MainViewModel : ViewModel() {

    lateinit var appList: List<PackageInfo>

    fun getAllPackageInfo(context: Context) {
        val packageManager = context.packageManager
        val list  = packageManager.getInstalledPackages(0)
        appList = list.sortedByDescending {
            it.lastUpdateTime
        }
        syncDb()
    }

    private fun syncDb() {
        viewModelScope.launch(Dispatchers.IO) {
            val allData = Dex2oatDatabase.getDb().packageInfoDao().getAll()
            if (allData.isEmpty()) {
                val list = appList.map {
                    PackageInfoEntity(
                        packageName = it.packageName,
                        versionCode = it.longVersionCode,
                        lastUpdateTime = it.lastUpdateTime,
                        isCompiled = false,
                        modifyTime = System.currentTimeMillis()
                    )
                }
                Dex2oatDatabase.getDb().packageInfoDao().insertAll(list)
            } else {
                val nowAllPackage = appList.map {
                    it.packageName
                }

                val dbAllPackage = allData.map {
                    it.packageName
                }

                val newPackage = nowAllPackage.subtract(dbAllPackage.toSet())
                val needDeletePackage = dbAllPackage.subtract(nowAllPackage.toSet())

                if (newPackage.isNotEmpty()) {
                    Dex2oatDatabase.getDb().packageInfoDao().insertAll(appList.filter {
                        newPackage.contains(it.packageName)
                    }.map {
                        PackageInfoEntity(
                            packageName = it.packageName,
                            versionCode = it.longVersionCode,
                            lastUpdateTime = it.lastUpdateTime,
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
}