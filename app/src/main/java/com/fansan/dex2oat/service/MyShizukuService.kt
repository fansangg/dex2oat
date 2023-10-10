package com.fansan.dex2oat.service

import android.util.Log
import com.fansan.dex2oat.IShizukuService
import kotlin.system.exitProcess

/**
 *@author  fansan
 *@version 2023/10/9
 */

class MyShizukuService:IShizukuService.Stub() {

    override fun destroy() {
        exitProcess(0)
    }

    override fun dex2oat(names: MutableList<String>) {
        Log.d("fansangg", "names == ${names.joinToString()}")
    }

}