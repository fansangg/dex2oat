package com.fansan.dex2oat

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.fansan.dex2oat.service.UserService
import com.fansan.dex2oat.ui.MainPage
import com.fansan.dex2oat.ui.theme.Dex2oatTheme
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {


    lateinit var userService: IUserService

    private val vm by viewModels<MainViewModel>()

    private val listener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 666) {
                initService()
            }
        }
    }


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service != null && service.pingBinder()) {
                Log.d("fansangg", "connect service")
                userService = IUserService.Stub.asInterface(service)
                lifecycleScope.launch {
                    vm.dex2oat(userService)
                }
            } else {
                Log.d("fansangg", "connect error")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            ToastUtils.showShort("disconnect ${name?.className}")
            Log.d("fansangg", "disconnect service")
        }

    }

    private val serviceArgs = Shizuku.UserServiceArgs(
        ComponentName(
            BuildConfig.APPLICATION_ID, UserService::class.java.name
        )
    ).daemon(false).processNameSuffix("dex2oat").debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Dex2oatTheme {
                MainPage(){
                    if (checkPermission(666))
                        initService()
                }
            }
        }
        vm.getAllPackageInfo(this)
        Shizuku.addRequestPermissionResultListener(listener)
    }

    private fun initService() {
        try {
            val ret = Shizuku.peekUserService(serviceArgs, connection)
            Log.d("fansangg", "peek == $ret")
            if (ret == -1) {
                Shizuku.bindUserService(serviceArgs, connection)
            }else{
                lifecycleScope.launch {
                    vm.dex2oat(userService)
                }
            }
        } catch (e: Exception) {
            Log.d("fansangg", "e == ${e.message}")
            ToastUtils.showShort("bind error ${e.message}")
        }
    }

    private fun checkPermission(code: Int): Boolean {
        if (Shizuku.isPreV11()) { // Pre-v11 is unsupported
            return false
        }
        return try {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                true
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                Log.d("fansangg", "授权shizuku")
                ToastUtils.showShort("请授权shizuku")
                false
            } else {
                Shizuku.requestPermission(code)
                false
            }
        } catch (e: Exception) {
            ToastUtils.showShort("${e.message}")
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Shizuku.unbindUserService(serviceArgs, connection, true)
        } catch (e: Exception) {
            Log.d("fansangg", "unbindservice ${e.message}")
        }
        Shizuku.removeRequestPermissionResultListener(listener)
    }
}