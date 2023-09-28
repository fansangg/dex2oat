package com.fansan.dex2oat

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fansan.dex2oat.dao.PackageInfoDao
import com.fansan.dex2oat.dao.PackageInfoEntity

/**
 *@author  fansan
 *@version 2023/9/28
 */

@Database(entities = [PackageInfoEntity::class], version = 1)
abstract class Dex2oatDatabase : RoomDatabase() {

    abstract fun packageInfoDao():PackageInfoDao

    companion object{

        private lateinit var db:Dex2oatDatabase

        fun init(context: Context){
            db = Room.databaseBuilder(context,Dex2oatDatabase::class.java,"dex2oat_db")
                .allowMainThreadQueries().build()
        }

        fun getDb():Dex2oatDatabase = db
    }
}