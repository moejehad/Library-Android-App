package com.example.library.ui.add

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.library.R
import com.example.library.databinding.FragmentAddBookBinding
import com.example.mylibrary.data.model.Book
import com.example.mylibrary.utils.Constant.COLLECTION_NAME
import com.example.mylibrary.utils.Constant.EMPTY_AUTHOR
import com.example.mylibrary.utils.Constant.EMPTY_NAME
import com.example.mylibrary.utils.Constant.EMPTY_PRICE
import com.example.mylibrary.utils.Constant.EMPTY_RATING
import com.example.mylibrary.utils.Constant.EMPTY_YEAR
import com.example.mylibrary.utils.Constant.ERROR
import com.example.mylibrary.utils.Constant.ERROR_MSG
import com.example.mylibrary.utils.Constant.STORAGE_FOLDER
import com.example.mylibrary.utils.Constant.UPLOAD_IMAGE
import com.example.mylibrary.utils.toastMessgae
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_add_book.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

class AddBookFragment : Fragment(R.layout.fragment_add_book), DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentAddBookBinding? = null
    private val binding get() = _binding!!
    private lateinit var mNavController: NavController
    private lateinit var db: FirebaseFirestore
    var date: String? = null
    lateinit var storge: FirebaseStorage
    lateinit var reference: StorageReference
    lateinit var progressDialog: ProgressDialog
    var path: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mNavController = findNavController()
        db = FirebaseFirestore.getInstance()

        storge = FirebaseStorage.getInstance()
        reference = storge!!.reference

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage(UPLOAD_IMAGE)
        progressDialog.setCancelable(false)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBookBinding.inflate(inflater, container, false)

        binding.LaunchYear.setOnClickListener {
            pickDate()
        }

        binding.AddBookBtn.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                addBookToFirebase(path)
            }
        }

        binding.BookImage.setOnClickListener {
            pickImageGallery()
        }

        return binding.root
    }

    val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {
            binding.BookImage.setImageURI(it)
            uploadImage(it)
        }
    )
    private fun pickImageGallery() {
        getImage.launch("image/*")
    }

    private fun uploadImage(uri: Uri?) {
        progressDialog.show()
        reference.child(STORAGE_FOLDER + UUID.randomUUID().toString()).putFile(uri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    path = uri.toString()
                }
                progressDialog.dismiss()
            }.addOnFailureListener { exception ->
                progressDialog.dismiss()
            }
    }

    private fun pickDate() {
        val cal = Calendar.getInstance()
        var day = cal.get(Calendar.DAY_OF_MONTH)
        var month = cal.get(Calendar.MONTH)
        var year = cal.get(Calendar.YEAR)

        DatePickerDialog(requireContext(), this, year, month, day).show()
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        date = "${p1}/${p2 + 1}/${p3}"
        LaunchYear.text = date
    }

    private fun addBookToFirebase(imagePath : String) {
        var bookName = editTextTextBookName.text
        var bookAuthor = editTextTextBookAuthor.text
        var bookYear = date
        var bookPrice = editTextTextPrice.text
        var bookRating = ratingBar.rating.toString()

        if (bookName.isEmpty()) {
            requireContext().toastMessgae(EMPTY_NAME)
        } else if (bookAuthor.isEmpty()) {
            requireContext().toastMessgae(EMPTY_AUTHOR)
        } else if (bookYear.isNullOrEmpty()) {
            requireContext().toastMessgae(EMPTY_YEAR)
        } else if (bookPrice.isEmpty()) {
            requireContext().toastMessgae(EMPTY_PRICE)
        } else if (ratingBar.rating == 0.0.toFloat()) {
            requireContext().toastMessgae(EMPTY_RATING)
        } else {

            val book = Book(
                bookName.toString(),
                bookAuthor.toString(),
                bookYear.toString(),
                bookRating,
                bookPrice.toString(),
                imagePath
            )

            try {
                db.collection(COLLECTION_NAME)
                    .add(book)
                    .addOnSuccessListener {
                        val action = AddBookFragmentDirections.actionAddBookFragmentToBooksFragment()
                        mNavController.navigate(action)
                        requireContext().toastMessgae("${bookName} added successfully")
                    }.addOnFailureListener { exception ->
                        requireContext().toastMessgae(ERROR_MSG)
                    }
            }catch (e : Exception){
                Log.e(ERROR, ERROR_MSG)
            }


        }

    }

}