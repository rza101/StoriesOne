package com.rhezarijaya.storiesone.data.room.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "stories")
@Parcelize
data class StoryEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo("photo_url")
    val photoUrl: String,

    @ColumnInfo("created_at")
    val createdAt: String,

    @ColumnInfo("name")
    val name: String,

    @ColumnInfo("description")
    val description: String,

    @ColumnInfo("lat")
    val lat: Double? = null,

    @ColumnInfo("lon")
    val lon: Double? = null,
) : Parcelable