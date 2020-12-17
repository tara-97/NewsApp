package com.example.android.newsapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.newsapp.database.NewsDatabaseObj
import com.example.android.newsapp.database.getDatabase
import com.example.android.newsapp.network.News
import com.example.android.newsapp.network.NewsApi
import com.example.android.newsapp.network.NewsApiService
import com.example.android.newsapp.network.NewsCountryFilter
import com.example.android.newsapp.repository.NewsRepository
import kotlinx.coroutines.launch
import java.lang.Exception

private const val TAG = "NewsViewModel"
class NewsViewModel(application: Application) : AndroidViewModel(application){

    private val database = getDatabase(application)
    private val repository = NewsRepository(database)
    val newsLive = repository.news


    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(NewsViewModel::class.java)){
                return NewsViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
    suspend fun getRecentLiveNews(filter:NewsCountryFilter){
        viewModelScope.launch {
            repository.getRecentNews(filter.value)

        }
    }


}
