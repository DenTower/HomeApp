package com.example.homeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// конфигурация базы данных
@Database(
    entities = [MemberEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun familyDao(): FamilyDao

}