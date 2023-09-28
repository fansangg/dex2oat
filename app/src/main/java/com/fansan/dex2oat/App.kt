package com.fansan.dex2oat

import android.app.Application
import android.content.Context

/**
 *@author  fansan
 *@version 2023/9/28
 */

class App:Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        app = this
    }

    override fun onCreate() {
        super.onCreate()
        Dex2oatDatabase.init(this)
    }

    companion object{
        lateinit var app: App
    }
}