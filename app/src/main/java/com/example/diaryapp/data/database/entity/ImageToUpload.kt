package com.example.diaryapp.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.diaryapp.util.Constants

@Entity(tableName = Constants.IMAGES_TO_UPLOAD)
data class ImageToUpload(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteImagePath: String,
    val imageUri: String,
    val sessionUri: String
)
