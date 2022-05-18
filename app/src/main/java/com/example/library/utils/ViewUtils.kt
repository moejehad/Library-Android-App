package com.example.mylibrary.utils

import android.content.Context
import android.widget.Toast

fun Context.toastMessgae(message:String){
    Toast.makeText(this,message,Toast.LENGTH_LONG).show()
}