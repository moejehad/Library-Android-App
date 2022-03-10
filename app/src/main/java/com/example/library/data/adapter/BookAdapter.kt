package com.example.mylibrary.data.adapter

import android.app.Activity
import android.app.DownloadManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.library.R
import com.example.mylibrary.data.model.Book
import com.example.mylibrary.utils.Constant.BOOK_YEAR
import com.example.mylibrary.utils.Constant.COLLECTION_NAME
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.book_item.view.*

class BookAdapter(val onClick : OnClickItem) {

    var db = Firebase.firestore
    var BookAdap: FirestoreRecyclerAdapter<Book, MyViewHolder>? = null
    lateinit var options: FirestoreRecyclerOptions<Book>

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookNumber = itemView.bookNumber
        val bookName = itemView.bookName
        val bookAuthor = itemView.bookAuthor
        val bookYear = itemView.bookYear
        val bookRating = itemView.bookRating
        val bookPrice = itemView.bookPrice
        val ratingNumber = itemView.ratingNumber
        val editBtn = itemView.editBtn
    }

    fun getBooks() {
        val query = db!!.collection(COLLECTION_NAME).orderBy(BOOK_YEAR,Query.Direction.ASCENDING)
        options = FirestoreRecyclerOptions.Builder<Book>().setQuery(query, Book::class.java).build()

        BookAdap = object : FirestoreRecyclerAdapter<Book, MyViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView =
                    LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
                return MyViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Book) {
                holder.bookNumber.text = (position+1).toString()
                holder.bookName.text = model.bookName
                holder.bookAuthor.text = model.bookAuthor
                holder.bookYear.text = model.bookYear
                holder.bookRating.rating = model.bookRating.toFloat()
                holder.bookPrice.text = "$ "+model.bookPrice
                holder.ratingNumber.text = model.bookRating
                holder.editBtn.setOnClickListener {
                    onClick.onClick(model)
                }
            }

        }
    }

    interface OnClickItem {
        fun onClick(book:Book)
    }

}