package com.fansan.dex2oat.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 *@author  fansan
 *@version 2023/9/28
 */

@Entity(
	tableName = "package_entity",
	indices = [Index(name = "indexPackageName", value = ["packageName"], unique = true)]
)
data class PackageInfoEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0L,

	@ColumnInfo val packageName: String,

	@ColumnInfo val versionCode: Long,

	@ColumnInfo val lastUpdateTime: Long,

	@ColumnInfo val isCompiled:Boolean,

	@ColumnInfo val modifyTime:Long
)