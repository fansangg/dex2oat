package com.fansan.dex2oat

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.fansan.dex2oat.service.UserService
import com.fansan.dex2oat.ui.theme.Dex2oatTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private val listener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 666) {
                initService()
            }
        }
    }

    lateinit var userService: IUserService

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service != null && service.pingBinder()) {
                Log.d("fansangg", "connect service")
                userService = IUserService.Stub.asInterface(service)
            } else {
                Log.d("fansangg", "connect error")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            ToastUtils.showShort("disconnect ${name?.className}")
            Log.d("fansangg", "disconnect service")
        }

    }

    private val args = Shizuku.UserServiceArgs(
        ComponentName(
            BuildConfig.APPLICATION_ID, UserService::class.java.name
        )
    ).daemon(false).processNameSuffix("dex2oat").debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkPermission(666)) initService()
        val vm by viewModels<MainViewModel>()
        vm.getAllPackageInfo(this)
        Shizuku.addRequestPermissionResultListener(listener)
        setContent {
            Dex2oatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    MainList() {
                        val result = userService.dex2oat(it)
                        Log.d("fansangg", "result == ${result.joinToString()}")
                    }
                }
            }
        }
    }

    private fun initService() {
        try {
            val ret = Shizuku.peekUserService(args, connection)
            Log.d("fansangg", "peek == $ret")
            if (ret == -1) {
                Shizuku.bindUserService(args, connection)
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
            Shizuku.unbindUserService(args, connection, true)
        } catch (e: Exception) {
            Log.d("fansangg", "unbindservice ${e.message}")
        }
        Shizuku.removeRequestPermissionResultListener(listener)
    }
}


@Composable
fun MainList(click: (packageName: String) -> Unit) {
    val vm = viewModel<MainViewModel>()
    LazyColumn(
        content = {
            items(vm.appList) {
                AppItem(info = it) {
                    click.invoke(it.packageName)
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
    )
}

@Composable
fun AppItem(info: PackageInfo, click: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .height(110.dp)
        .background(
            color = MaterialTheme.colorScheme.primary.copy(alpha = .6f),
            shape = RoundedCornerShape(15.dp)
        )
        .padding(horizontal = 12.dp, vertical = 8.dp)
        .clickable {
            click()
        }) {
        Row(Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
            Image(
                painter = rememberDrawablePainter(drawable = AppUtils.getAppIcon(info.packageName)),
                contentDescription = "icon",
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.Fit
            )

            SpacerW(width = 10.dp)

            Column(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = AppUtils.getAppName(info.packageName), fontSize = 11.sp)
                Text(
                    text = info.packageName,
                    fontSize = 11.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Row {
                    Text(
                        text = info.versionName,
                        fontSize = 11.sp,
                        modifier = Modifier.alignByBaseline()
                    )
                    SpacerW(width = 4.dp)
                    Text(
                        text = info.longVersionCode.toString(),
                        fontSize = 9.sp,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }
        }

        SpacerH(height = 8.dp)
        Column {
            Text(
                text = "首次安装日期:${TimeUtils.millis2String(info.firstInstallTime)}",
                fontSize = 11.sp
            )
            SpacerH(height = 4.dp)
            Text(
                text = "更新时间:${TimeUtils.millis2String(info.lastUpdateTime)}", fontSize = 11.sp
            )
        }
    }
}