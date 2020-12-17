package com.example.android.newsapp.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NewsDao {
    @Query("select * from newstable ORDER BY date(timeStamp) DESC")
    fun getNews(): LiveData<List<NewsDatabaseObj>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg news: NewsDatabaseObj)
}
@Database(entities = [NewsDatabaseObj::class], exportSchema = false,version = 1)
abstract class NewsDatabase : RoomDatabase() {
    abstract val newsDao: NewsDao
}

private lateinit var INSTANCE: NewsDatabase

fun getDatabase(context: Context): NewsDatabase{
    synchronized(NewsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                NewsDatabase::class.java,
                "news").build()
        }
    }
    return INSTANCE
}