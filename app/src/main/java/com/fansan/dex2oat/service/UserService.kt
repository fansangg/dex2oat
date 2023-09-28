package com.fansan.dex2oat.service

import android.util.Log
import com.blankj.utilcode.util.ShellUtils
import com.fansan.dex2oat.IUserService
import kotlinx.coroutines.MainScope
import kotlin.system.exitProcess

/**
 *@author  fansan
 *@version 2023/9/28
 */

class UserService : IUserService.Stub() {

    override fun destroy() {
        exitProcess(0)
    }

    override fun doSomething() {
        Log.d("fansangg", "do something")
    }

    override fun dex2oat(packageName:String):List<String>{
        val result = ShellUtils.execCmd("cmd package compile -m speed -f $packageName", false)

        return listOf(result.successMsg,result.errorMsg)
    }

    override fun exit() {
        destroy()
    }

}