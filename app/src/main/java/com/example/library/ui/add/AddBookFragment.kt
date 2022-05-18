package com.example.library.ui.add

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.activity.result.ActivityResultCallback
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
import com.example.mylibrary.utils.Constant.STORAGE_FOLDER_VIDEIOS
import com.example.mylibrary.utils.Constant.UPLOAD
import com.example.mylibrary.utils.toastMessgae
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_add_book.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class AddBookFragment : Fragment(R.layout.fragment_add_book), DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentAddBookBinding? = null
    private val binding get() = _binding!!
    private lateinit var mNavController: NavController
    private lateinit var database: DatabaseReference
    var date: String? = null
    lateinit var storge: FirebaseStorage
    lateinit var reference: StorageReference
    lateinit var progressDialog: ProgressDialog
    var path: String = ""
    var videoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://my-library-d6912-default-rtdb.firebaseio.com/Books")
        storge = FirebaseStorage.getInstance()
        reference = storge!!.reference

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage(UPLOAD)
        progressDialog.setCancelable(false)

        mNavController = findNavController()

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
                addBookToFirebase(path,videoPath)
            }
        }

        binding.BookImage.setOnClickListener {
            pickImageGallery()
        }

        binding.UploadVideo.setOnClickListener {
            pickVideoGallery()
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(COLLECTION_NAME, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            Log.w(COLLECTION_NAME,token)
        })

        return binding.root
    }

    val getVideo = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {
            uploadVideo(it)
        }
    )

    private fun pickVideoGallery() {
        getVideo.launch("video/*")
    }

    private fun uploadVideo(uri: Uri?) {
        progressDialog.show()
        reference.child(STORAGE_FOLDER_VIDEIOS + UUID.randomUUID().toString()).putFile(uri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    videoPath = uri.toString()
                    UploadSuccess.visibility = View.VISIBLE
                }
                progressDialog.dismiss()
            }.addOnFailureListener { exception ->
                progressDialog.dismiss()
            }
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

    private fun addBookToFirebase(imagePath: String , video: String) {
        var bookId = database.push().key.toString()
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
                bookId,
                bookName.toString(),
                bookAuthor.toString(),
                bookYear.toString(),
                bookRating,
                bookPrice.toString(),
                imagePath,
                video
            )

            try {
                database.child(bookId).setValue(book)
                    .addOnSuccessListener {
                        val action =
                            AddBookFragmentDirections.actionAddBookFragmentToBooksFragment()
                        mNavController.navigate(action)
                        requireContext().toastMessgae("${bookName} added successfully")
                    }.addOnFailureListener { exception ->
                        requireContext().toastMessgae(ERROR_MSG)
                    }
            } catch (e: Exception) {
                Log.e(ERROR, ERROR_MSG)
            }


        }

    }

}