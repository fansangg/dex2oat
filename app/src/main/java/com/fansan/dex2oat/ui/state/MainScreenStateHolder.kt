package com.fansan.dex2oat.ui.state

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainScreenStateHolder {
    val loadingPackage = mutableStateOf(false)
    var selecteNum by mutableIntStateOf(0)
    var currentProcessIndex by mutableIntStateOf(0)
    var processCount = 0
    var currentProcessName by mutableStateOf("")
    var running by mutableStateOf(false)
	var checkCompiled by mutableStateOf(false)
	var checkSystemApp by mutableStateOf(false)

    val selectMode: State<Boolean>
        get() = derivedStateOf { selecteNum != 0 }

    fun showLoadingPackage(){
        loadingPackage.value = true
    }

    fun hideLoadingPackage(){
        loadingPackage.value = false
    }
}