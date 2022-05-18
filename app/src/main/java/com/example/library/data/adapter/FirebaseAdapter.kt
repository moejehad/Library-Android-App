package com.example.library.data.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.library.R
import com.example.mylibrary.data.adapter.BookAdapter
import com.example.mylibrary.data.model.Book
import kotlinx.android.synthetic.main.book_item.view.*

class FirebaseAdapter(val list:ArrayList<Book> , val onClick : OnClickItem) : RecyclerView.Adapter<FirebaseAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookImage = itemView.bookImage
        val bookName = itemView.bookName
        val bookAuthor = itemView.bookAuthor
        val bookYear = itemView.bookYear
        val bookRating = itemView.bookRating
        val bookPrice = itemView.bookPrice
        val ratingNumber = itemView.ratingNumber
        val editBtn = itemView.editBtn
        val previewBtn = itemView.previewBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        return FirebaseAdapter.MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(holder.itemView).load(list.get(position).bookImage).placeholder(R.drawable.ic_upload)
            .into(holder.bookImage)
        holder.bookName.text = list.get(position).bookName
        holder.bookAuthor.text = list.get(position).bookAuthor
        holder.bookYear.text = list.get(position).bookYear
        holder.bookRating.rating = list.get(position).bookRating.toFloat()
        holder.bookPrice.text = "$ " + list.get(position).bookPrice
        holder.ratingNumber.text = list.get(position).bookRating
        holder.editBtn.setOnClickListener {
            onClick.onItemClick(list.get(position))
        }
        holder.previewBtn.setOnClickListener {
            onClick.onVideoClick(list.get(position))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickItem {
        fun onItemClick(book: Book)
        fun onVideoClick(book: Book)
    }

}