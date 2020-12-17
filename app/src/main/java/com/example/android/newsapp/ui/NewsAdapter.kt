package com.example.android.newsapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.newsapp.databinding.NewsItemBinding
import com.example.android.newsapp.network.News

private const val TAG = "NewsAdapter"
class NewsAdapter(val clickListener: OnClickListener) : ListAdapter<News,NewsAdapter.NewsItemViewHolder>(NewsItemDiffCallback()) {

    class NewsItemViewHolder private constructor(val binding:NewsItemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(item: News, clickListener: OnClickListener){

            binding.news = item
            binding.clickListener = clickListener
            binding.executePendingBindings()


        }
        companion object {
            fun from(parent: ViewGroup) : NewsItemViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = NewsItemBinding.inflate(layoutInflater,parent,false)
                return NewsItemViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsItemViewHolder {
        return NewsItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: NewsItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item,clickListener)
    }
}

class NewsItemDiffCallback : DiffUtil.ItemCallback<News>() {
    override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
        return  oldItem == newItem
    }
}
class OnClickListener(val clickListener : (newsUrl:String) -> Unit){
    fun onClick(news:News){clickListener(news.url)}
}