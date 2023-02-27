package com.example.diaryapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.diaryapp.data.database.ImageToUploadDao
import com.example.diaryapp.data.database.entity.ImageToDelete
import com.example.diaryapp.data.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 3,
    exportSchema = false
)
abstract class ImagesDatabase: RoomDatabase() {
    abstract fun imageToUploadDao(): ImageToUploadDao
    abstract fun imageToDeleteDao(): ImageToDeleteDao

}