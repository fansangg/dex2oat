package com.fansan.dex2oat.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ShellUtils
import com.fansan.dex2oat.MainActivity
import com.fansan.dex2oat.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Dex2oatService : Service() {

	private val NOTIFYCATION_ID = 666
	private val CHANNEL_ID = "dex2oat"
	private lateinit var notificationManager: NotificationManager
	private lateinit var notificationBuilder: NotificationCompat.Builder

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}

	override fun onCreate() {
		super.onCreate()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		createNotifyCationChannel()
		createNotification()
		startForeground(NOTIFYCATION_ID, notificationBuilder.build())
		intent?.let {
			GlobalScope.launch(Dispatchers.IO){
				val list = it.getStringArrayExtra("names")
				list?.forEachIndexed { index, s ->
					updateNotifycation(s, list.size, index + 1)
					val result = ShellUtils.execCmd("ls -la", listOf("${PathUtils.getExternalAppFilesPath()}/rish"), false)
					if (result.successMsg == "Success") {
						Log.d("fansangg", "$s 成功")
					}else{
						Log.d("fansangg", "result.errorMsg == ${result.errorMsg}")
					}
				}
			}
		}
		return START_NOT_STICKY
	}

	private fun createNotification() {
		val i = Intent(this, MainActivity::class.java)
		val pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE)
		notificationBuilder =
			NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("编译服务运行中")
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setSmallIcon(R.drawable.ic_launcher_foreground).setAutoCancel(false)
				.setOnlyAlertOnce(true).setContentIntent(pendingIntent)
	}

	private fun updateNotifycation(content: String, max:Int,progress: Int) {
		if (::notificationBuilder.isInitialized) {
			notificationManager.notify(
				NOTIFYCATION_ID,
				notificationBuilder.setContentText(content).setProgress(max, progress, false)
					.build()
			)
		}

	}

	override fun onDestroy() {
		super.onDestroy()

	}

	private fun createNotifyCationChannel() {
		val channel =
			NotificationChannel(CHANNEL_ID, "编译服务", NotificationManager.IMPORTANCE_DEFAULT)
		notificationManager = getSystemService(NotificationManager::class.java)
		notificationManager.createNotificationChannel(channel)
	}

}