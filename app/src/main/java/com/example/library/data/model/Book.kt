package com.example.mylibrary.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Book(
    var bookName: String = "",
    var bookAuthor: String = "",
    var bookYear: String = "",
    var bookRating: String = "",
    var bookPrice:String = "",
    var bookImage : String = ""
) : Parcelable
