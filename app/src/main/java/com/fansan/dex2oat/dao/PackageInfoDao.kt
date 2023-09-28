package com.fansan.dex2oat.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 *@author  fansan
 *@version 2023/9/28
 */


@Dao
interface PackageInfoDao {

    @Query("select * from package_entity")
    fun getAll():List<PackageInfoEntity>

    @Query("select * from package_entity where packageName like :packageName")
    fun getEntity(packageName:String):PackageInfoEntity

    @Insert
    fun insertEntity(entity: PackageInfoEntity)

    @Insert
    fun insertAll(entity: List<PackageInfoEntity>)

    @Update
    fun updateEntity(entity: PackageInfoEntity)

    @Update
    fun updateAll(entity: List<PackageInfoEntity>)

    @Delete
    fun deleteEntity(entity: List<PackageInfoEntity>)

}