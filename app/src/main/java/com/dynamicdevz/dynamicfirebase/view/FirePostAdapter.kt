package com.dynamicdevz.dynamicfirebase.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dynamicdevz.dynamicfirebase.databinding.PostItemLayoutBinding
import com.dynamicdevz.dynamicfirebase.model.FirePost

class FirePostAdapter : RecyclerView.Adapter<FirePostAdapter.FirePostViewHolder>() {

    inner class FirePostViewHolder(val binding: PostItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    var fireList = listOf<FirePost>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirePostViewHolder {
        val binding = PostItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return FirePostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FirePostViewHolder, position: Int) {

        val post = fireList[position]

        holder.binding.apply {
            captionTextview.text = post.caption

            Glide.with(holder.itemView)
                .applyDefaultRequestOptions(RequestOptions().centerCrop())
                .load(post.imageUrl)
                .into(pictureImageview)
            
            pictureImageview.visibility = if(post.imageUrl.isEmpty()) View.GONE else View.VISIBLE
        }
    }
    override fun getItemCount(): Int = fireList.size
}