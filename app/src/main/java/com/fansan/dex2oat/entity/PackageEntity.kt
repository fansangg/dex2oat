package com.fansan.dex2oat.entity

import android.content.pm.PackageInfo
import androidx.compose.runtime.Stable

@Stable
data class PackageEntity(val info:PackageInfo,val isSelected:Boolean = false)