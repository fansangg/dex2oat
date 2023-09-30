package com.fansan.dex2oat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.fansan.dex2oat.MainActivity
import com.fansan.dex2oat.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Dex2oatService:Service() {

	private val NOTIFYCATION_ID = 666
	private val CHANNEL_ID = "dex2oat"
	private lateinit var notificationManager: NotificationManager

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}

	override fun onCreate() {
		super.onCreate()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		createNotifyCationChannel()
		startForeground(NOTIFYCATION_ID,getNotifycation("哈哈哈",20))
		GlobalScope.launch(Dispatchers.IO){
			delay(4000)
			updateNotifycation("我更新了",60)
		}
		return START_NOT_STICKY
	}

	private fun getNotifycation(content: String, progress: Int): Notification {
		val i = Intent(this, MainActivity::class.java)
		val pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE)

		return NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("编译服务运行中")
			.setContentText(content).setPriority(NotificationCompat.PRIORITY_HIGH)
			.setSmallIcon(R.drawable.ic_launcher_foreground).setProgress(100, progress, false)
			.setContentIntent(pendingIntent).build()
	}

	fun updateNotifycation(content: String,progress: Int){
		notificationManager.notify(NOTIFYCATION_ID,getNotifycation(content, progress))
	}

	override fun onDestroy() {
		super.onDestroy()

	}

	private fun createNotifyCationChannel(){
		val channel = NotificationChannel(CHANNEL_ID, "编译服务", NotificationManager.IMPORTANCE_DEFAULT)
		notificationManager = getSystemService(NotificationManager::class.java)
		notificationManager.createNotificationChannel(channel)
	}

}