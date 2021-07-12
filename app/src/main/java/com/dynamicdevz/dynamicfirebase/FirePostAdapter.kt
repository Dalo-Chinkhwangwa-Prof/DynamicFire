package com.dynamicdevz.dynamicfirebase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dynamicdevz.dynamicfirebase.databinding.PostItemLayoutBinding

class FirePostAdapter: RecyclerView.Adapter<FirePostAdapter.FirePostViewHolder>() {

    inner class FirePostViewHolder(val binding: PostItemLayoutBinding): RecyclerView.ViewHolder(binding.root)

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

        }


    }

    override fun getItemCount(): Int = fireList.size
}