package com.fansan.dex2oat.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import com.fansan.dex2oat.BuildConfig
import com.fansan.dex2oat.IShizukuService
import com.fansan.dex2oat.MainActivity
import com.fansan.dex2oat.R
import rikka.shizuku.Shizuku


class Dex2oatService : Service() {

	private val NOTIFYCATION_ID = 666
	private val CHANNEL_ID = "dex2oat"
	private lateinit var notificationManager: NotificationManager
	private lateinit var notificationBuilder: NotificationCompat.Builder
	private var maxLength = 0
	private lateinit var names:List<String>
	private lateinit var remoteViews: RemoteViews
	private lateinit var handler: Handler
	private lateinit var wakeLock: WakeLock

	private val userServiceArgs = Shizuku.UserServiceArgs(
		ComponentName(BuildConfig.APPLICATION_ID, MyShizukuService::class.java.name)
	).daemon(false).processNameSuffix("dex2oat").debuggable(BuildConfig.DEBUG)
		.version(BuildConfig.VERSION_CODE)

	private val userServiceConnection: ServiceConnection = object : ServiceConnection {
		override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
			Log.d("fansangg", "onServiceConnected -- ${componentName.className} ")
			if (binder.pingBinder()) {
				val service: IShizukuService = IShizukuService.Stub.asInterface(binder)
				/*GlobalScope.launch(Dispatchers.IO) {
					names.forEachIndexed { index, s ->
						//updateNotifycation(s,index+1)
						Log.d("fansangg", "index == $index s == $s")
						delay(1000)
					}

					stopForeground(STOP_FOREGROUND_REMOVE)
					stopSelf()
				}*/
			}
		}

		override fun onServiceDisconnected(componentName: ComponentName) {
			Log.d(
				"fansangg", """
	        	onServiceDisconnected: 
	        	${componentName.className}
	        	""".trimIndent()
			)
		}
	}

	override fun onBind(intent: Intent?): IBinder? {
		return Binder()
	}

	override fun onCreate() {
		super.onCreate()
		createNotifyCationChannel()
		createNotification()
	}

	override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
		if (intent.extras == null) stopSelf()
		names = intent.extras?.getStringArray("names")!!.toList()
		maxLength = names.size
		Shizuku.bindUserService(userServiceArgs,userServiceConnection)
		wakeLock.acquire()
		handler.post {
			while (true){
				Log.d("fansangg", "-----111")
				SystemClock.sleep(1000)
			}
		}
		startForeground(NOTIFYCATION_ID, notificationBuilder.build())
		return super.onStartCommand(intent, flags, startId)
	}

	private fun createNotification() {
		val i = Intent(this, MainActivity::class.java)
		remoteViews = RemoteViews(packageName,R.layout.custom_notification)
		val pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE)
		notificationBuilder =
			NotificationCompat.Builder(this, CHANNEL_ID).
					setStyle(NotificationCompat.DecoratedCustomViewStyle())
				.setCustomContentView(remoteViews)
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setOngoing(true)
				.setCategory(NotificationCompat.CATEGORY_MESSAGE)
				.setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
				.setSmallIcon(R.drawable.ic_launcher_foreground).setAutoCancel(false)
				.setContentIntent(pendingIntent)
	}

	private fun updateNotifycation(content: String, progress: Int) {
		if (::notificationBuilder.isInitialized) {
			remoteViews.setTextViewText(R.id.packagename,content)
			remoteViews.setProgressBar(R.id.pb,maxLength,progress,false)
			val currentProgress = (progress / maxLength.toFloat()) * 100
			remoteViews.setTextViewText(R.id.progress,"$currentProgress%")
			startForeground(NOTIFYCATION_ID,notificationBuilder.build())
		}

	}

	override fun onDestroy() {
		super.onDestroy()
		stopForeground(STOP_FOREGROUND_REMOVE)
	}

	private fun createNotifyCationChannel() {
		val channel =
			NotificationChannel(CHANNEL_ID, "编译服务", NotificationManager.IMPORTANCE_LOW)
		channel.enableVibration(false)
		channel.enableLights(false)
		channel.setShowBadge(false)
		notificationManager = getSystemService(NotificationManager::class.java)
		notificationManager.createNotificationChannel(channel)
	}

}