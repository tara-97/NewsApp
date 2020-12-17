package com.example.android.newsapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.newsapp.network.News
import org.jetbrains.annotations.NotNull

@Entity(tableName = "newstable")
data class NewsDatabaseObj(
                        @PrimaryKey @NotNull
                        val url:String,
                        val author :String? ,
                        val title:String? ,
                        val description:String? ,
                        val imgUrl:String?,
                        val timeStamp:String?
                        )

fun List<NewsDatabaseObj>.asDomainModel() : List<News>{

    return map {
        News(it.author,it.title,it.description,it.imgUrl,it.url,it.timeStamp)
    }
}