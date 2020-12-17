package com.example.android.newsapp.network

import com.example.android.newsapp.database.NewsDatabaseObj
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewsListContainer(val articles:List<News>)

@JsonClass(generateAdapter = true)
data class News(
    val author :String? = "",
    val title:String? = "",
    val description:String? = "",
    @Json(name = "urlToImage")
    val imgUrl:String?,
    val url:String,
    val publishedAt:String?
)
fun NewsListContainer.asDatabaseModel() : Array<NewsDatabaseObj>{
    return articles.map {
        NewsDatabaseObj(it.url,it.author,it.title,it.description,it.imgUrl,it.publishedAt)
    }.toTypedArray()
}