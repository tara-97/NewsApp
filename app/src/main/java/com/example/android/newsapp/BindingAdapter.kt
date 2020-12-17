
package com.example.android.newsapp

import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.newsapp.network.News
import com.example.android.newsapp.ui.NewsAdapter

private const val TAG = "BindingAdapter"
@BindingAdapter("listData")
fun RecyclerView.bindNewsAdapter(news:List<News>?){
    Log.d(TAG, "bindNewsAdapter: Called ")
    val adapter = this.adapter as NewsAdapter
    adapter.submitList(news)


}
@BindingAdapter("imgScrUrl")
fun bindImage(imgView: ImageView, imgUrl:String?){
    imgUrl?.let {
        val imgUri = it.toUri().buildUpon().scheme("https").build()
        Glide
                .with(imgView.context)
                .load(imgUri)
                .apply(RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image))
                .into(imgView)
    }
}