package com.fansan.dex2oat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.fansan.dex2oat.dao.PackageInfoEntity
import com.fansan.dex2oat.entity.PackageEntity
import com.fansan.dex2oat.service.Dex2oatService
import com.fansan.dex2oat.ui.state.MainScreenStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *@author  fansan
 *@version 2023/9/27
 */

class MainViewModel : ViewModel() {

    private val appList = mutableListOf<PackageEntity>()
	var currentShowList by mutableStateOf(emptySet<PackageEntity>())
    val stateHolder = MainScreenStateHolder()

    fun getAllPackageInfo() {
        stateHolder.showLoadingPackage()
        val packageManager = App.app.packageManager
        val list = packageManager.getInstalledPackages(0)
        appList.addAll(list.map {
            PackageEntity(it, false)
        })
        viewModelScope.launch {
            syncDb()
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
                        modifyTime = 0L
                    )
                }
                Dex2oatDatabase.getDb().packageInfoDao().insertAll(list)
            } else {
				//更新是否编译
				appList.forEach {
					allData.find { entity ->
						entity.packageName == it.info.packageName
					}?.let { entity ->
						if (entity.isCompiled){
							if (entity.lastUpdateTime != it.info.lastUpdateTime){
								Dex2oatDatabase.getDb().packageInfoDao().updateEntity(entity.copy(isCompiled = false, modifyTime = 0L))
							}
						}
					}
				}

	            //添加新的package和移除不存在的package
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
                            modifyTime = 0L
                        )
                    })
                }

                if (needDeletePackage.isNotEmpty()) {
                    Dex2oatDatabase.getDb().packageInfoDao().deleteEntity(allData.filter {
                        needDeletePackage.contains(it.packageName)
                    })
                }
            }

	        stateHolder.hideLoadingPackage()
	        updateList()
        }
    }

	fun updateList(){
		currentShowList = appList.filter {
			if (stateHolder.checkCompiled && stateHolder.checkSystemApp)
				Dex2oatDatabase.getDb().packageInfoDao().getEntity(it.info.packageName).isCompiled
			else if (stateHolder.checkCompiled){
				Dex2oatDatabase.getDb().packageInfoDao().getEntity(it.info.packageName).isCompiled && AppUtils.isAppSystem(it.info.packageName).not()
			}else if (stateHolder.checkSystemApp){
				true
			}else{
				AppUtils.isAppSystem(it.info.packageName).not()
			}
		}.sortedByDescending {
			it.info.lastUpdateTime
		}.toSet()
	}

	fun toggleSelectAll(){
		val allSelected = currentShowList.all { it.isSelected }
		currentShowList = currentShowList.map {
			it.copy(isSelected = !allSelected)
		}.toSet()
	}



	fun dex2oat(){
		val packageNameList = currentShowList.filter {
			it.isSelected
		}.map {
			it.info.packageName
		}.toTypedArray()

        /*withContext(Dispatchers.IO){
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
        }*/

		val serviceIntent = Intent(App.app,Dex2oatService::class.java)
		serviceIntent.putExtra("names",packageNameList)
		App.app.startForegroundService(serviceIntent)
    }

	@SuppressLint("BatteryLife")
	fun checkPower(activity: Activity) {
		val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
		val ignore = powerManager.isIgnoringBatteryOptimizations(App.app.packageName)
		if (!ignore) {
			val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
			intent.data = Uri.parse("package:${activity.packageName}")
			activity.startActivity(intent)
		}else{
			dex2oat()
		}
	}
}