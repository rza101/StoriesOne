package com.rhezarijaya.storiesone.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rhezarijaya.storiesone.data.room.dao.RemoteKeysDao
import com.rhezarijaya.storiesone.data.room.dao.StoriesDao
import com.rhezarijaya.storiesone.data.room.entity.RemoteKeys
import com.rhezarijaya.storiesone.data.room.entity.StoryEntity

@Database(
    entities = [StoryEntity::class, RemoteKeys::class],
    version = 1,
)
abstract class StoriesDatabase : RoomDatabase() {
    abstract fun getRemoteKeysDao(): RemoteKeysDao
    abstract fun getStoriesDao(): StoriesDao

    companion object {
        private const val DATABASE_NAME = "db_stories"

        @Volatile
        private var instance: StoriesDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): StoriesDatabase {
            if (instance == null) {
                synchronized(this) {
                    instance = Room.databaseBuilder(
                        context,
                        StoriesDatabase::class.java,
                        DATABASE_NAME
                    ).build()
                }
            }

            return instance!!
        }
    }
}