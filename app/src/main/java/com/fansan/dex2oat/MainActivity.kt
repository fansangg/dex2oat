package com.fansan.dex2oat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.fansan.dex2oat.ui.MainPage
import com.fansan.dex2oat.ui.theme.Dex2oatTheme
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Dex2oatTheme {
                MainPage()
            }
        }

	    moveRish2Path()
    }

	private fun moveRish2Path(){
		val rishFile = File(PathUtils.getExternalAppFilesPath(),"rish")
		val rishShizukuFile = File(PathUtils.getExternalAppFilesPath(),"rish_shizuku.dex")
		if (rishFile.exists() && rishShizukuFile.exists()){
			Log.d("fansangg", "rish exists")
			return
		}
		FileUtils.createOrExistsFile(rishFile)
		FileUtils.createOrExistsFile(rishShizukuFile)
		val rishIS = assets.open("rish")
		val shizukuIS = assets.open("rish_shizuku.dex")
		FileIOUtils.writeFileFromIS(rishFile,rishIS)
		FileIOUtils.writeFileFromIS(rishShizukuFile,shizukuIS)
		Log.d("fansangg", "move done")
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		Log.d("fansangg", "result code == $resultCode")
	}
}