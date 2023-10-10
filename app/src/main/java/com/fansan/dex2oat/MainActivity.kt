package com.fansan.dex2oat

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.fansan.dex2oat.service.Dex2oatService
import com.fansan.dex2oat.ui.MainPage
import com.fansan.dex2oat.ui.theme.Dex2oatTheme
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

	private val shizukuListener = ::onRequestPermissionResult
	private val vm:MainViewModel by viewModels<MainViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			Dex2oatTheme {
				MainPage(){
					if (checkPermission()) bindService()
				}
			}
		}

		Shizuku.addRequestPermissionResultListener(shizukuListener)
	}

	override fun onDestroy() {
		super.onDestroy()
		Shizuku.removeRequestPermissionResultListener(shizukuListener)
	}

	private fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
		if (requestCode == 666 && grantResult == PackageManager.PERMISSION_GRANTED) {
			Log.d("fansangg", "shizuku授权成功")
			bindService()
		}
	}

	private fun checkPermission(): Boolean {
		if (Shizuku.isPreV11()) {
			return false
		}
		try {
			return if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
				true
			} else if (Shizuku.shouldShowRequestPermissionRationale()) {
				false
			} else {
				Shizuku.requestPermission(666)
				false
			}
		} catch (e: Throwable) {
			Log.d("fansangg", "shizuku error = ${e.message}")
		}
		return false
	}

	private fun bindService() {
		val intent = Intent(this,Dex2oatService::class.java)
		val list = vm.currentShowList.filter {
			it.isSelected
		}.map {
			it.info.packageName
		}
		intent.putExtra("names",list.toTypedArray())
		startForegroundService(intent)
	}
}