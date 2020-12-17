package com.example.android.newsapp.repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.android.newsapp.database.NewsDatabase
import com.example.android.newsapp.database.asDomainModel
import com.example.android.newsapp.network.News
import com.example.android.newsapp.network.NewsApi
import com.example.android.newsapp.network.asDatabaseModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "NewsRepository"
class NewsRepository(private val database: NewsDatabase) {

    val news: LiveData<List<News>> = Transformations.map(database.newsDao.getNews()){
        it.asDomainModel()
    }

    suspend fun getRecentNews(country:String){

        try{
            withContext(Dispatchers.IO) {

                val newsList = NewsApi.retrofitService.getTopHeadline(country,"8ec1c25bfc744dab8ab094e6a99c551c")
                database.newsDao.insertAll(*newsList.asDatabaseModel())




            }

        }catch (e:Exception){
            Log.d(TAG, "Error: ${e.message}")


        }

    }

}

