package com.example.library.ui.edit

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
import com.bumptech.glide.Glide
import com.example.library.R
import com.example.library.databinding.FragmentEditBookBinding
import com.example.mylibrary.data.model.Book
import com.example.mylibrary.utils.Constant
import com.example.mylibrary.utils.Constant.BOOK_AUTHOR
import com.example.mylibrary.utils.Constant.BOOK_DATA
import com.example.mylibrary.utils.Constant.BOOK_NAME
import com.example.mylibrary.utils.Constant.COLLECTION_NAME
import com.example.mylibrary.utils.Constant.DELETED
import com.example.mylibrary.utils.Constant.ERROR
import com.example.mylibrary.utils.Constant.ERROR_MSG
import com.example.mylibrary.utils.Constant.STORAGE_FOLDER
import com.example.mylibrary.utils.Constant.UPDATE
import com.example.mylibrary.utils.Constant.UPLOAD
import com.example.mylibrary.utils.toastMessgae
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_add_book.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class EditBookFragment : Fragment(R.layout.fragment_edit_book), DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentEditBookBinding? = null
    private val binding get() = _binding!!
    private lateinit var mNavController: NavController
    private lateinit var database: DatabaseReference
    private lateinit var book: Book
    lateinit var storge: FirebaseStorage
    lateinit var reference: StorageReference
    var date: String? = null
    var path: String = ""
    var videoPath: String = ""
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mNavController = findNavController()
        database = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://my-library-d6912-default-rtdb.firebaseio.com/Books")
        book = arguments?.getParcelable<Book>(BOOK_DATA)!!

        storge = FirebaseStorage.getInstance()
        reference = storge!!.reference

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage(UPLOAD)
        progressDialog.setCancelable(false)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditBookBinding.inflate(inflater, container, false)

        binding.editTextTextBookName.setText(book.bookName)
        binding.editTextTextBookAuthor.setText(book.bookAuthor)
        binding.LaunchYear.setText(book.bookYear)
        binding.editTextTextPrice.setText(book.bookPrice)
        binding.ratingBar.rating = book.bookRating.toFloat()
        Glide.with(requireParentFragment()).load(book.bookImage).placeholder(R.drawable.ic_upload)
            .into(binding.EditBookImage)

        binding.LaunchYear.setOnClickListener {
            pickDate()
        }

        binding.EditBookImage.setOnClickListener {
            editBookImage()
        }

        binding.EditUploadVideo.setOnClickListener {
            pickVideoGallery()
        }

        binding.EditBookBtn.setOnClickListener {
            GlobalScope.launch {
                if (path.isNotEmpty()) {
                    EditBookFromFirestore(path,book.bookVideo)
                }else if (videoPath.isNotEmpty()){
                    EditBookFromFirestore(book.bookImage,videoPath)
                } else {
                    EditBookFromFirestore(book.bookImage,book.bookVideo)
                }
            }
        }

        binding.DeleteBookBtn.setOnClickListener {
            GlobalScope.launch {
                DeleteBookFromFirestore()
            }
        }

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
        reference.child(Constant.STORAGE_FOLDER_VIDEIOS + UUID.randomUUID().toString()).putFile(uri!!)
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
            binding.EditBookImage.setImageURI(it)
            uploadImage(it)
        }
    )

    private fun uploadImage(it: Uri?) {
        progressDialog.show()
        reference.child(STORAGE_FOLDER + UUID.randomUUID().toString()).putFile(it!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    path = uri.toString()
                }
                progressDialog.dismiss()
            }.addOnFailureListener { exception ->
                progressDialog.dismiss()
            }
    }

    private fun editBookImage() {
        getImage.launch("image/*")
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

    private fun EditBookFromFirestore(newPath: String , video: String) {
        var editBookName = editTextTextBookName.text
        var editBookAuthor = editTextTextBookAuthor.text
        var editBookYear = LaunchYear.text
        var editBookPrice = editTextTextPrice.text
        var editBookRating = ratingBar.rating.toString()

        if (editBookName.isNotEmpty() && editBookAuthor.isNotEmpty() && editBookYear.isNotEmpty()
            && editBookPrice.isNotEmpty() && ratingBar.rating !== 0.0.toFloat()
        ) {

            val EditBook = Book(
                book.bookID,
                editBookName.toString(),
                editBookAuthor.toString(),
                editBookYear.toString(),
                editBookRating,
                editBookPrice.toString(),
                newPath,
                video
            )

            try {
                database.child(book.bookID).setValue(EditBook).addOnSuccessListener { it ->
                    val action =
                        EditBookFragmentDirections.actionEditBookFragmentToBooksFragment()
                    mNavController.navigate(action)
                    requireActivity().toastMessgae(UPDATE)
                }.addOnFailureListener {
                    requireActivity().toastMessgae(ERROR_MSG)
                }
            } catch (e: Exception) {
                Log.e(ERROR, ERROR_MSG)
            }
        } else {
            Log.e(ERROR, ERROR_MSG)
        }
    }

    private fun DeleteBookFromFirestore() {
        try {
            database.child(book.bookID).setValue(null).addOnSuccessListener { it ->
                val action = EditBookFragmentDirections.actionEditBookFragmentToBooksFragment()
                mNavController.navigate(action)
                requireActivity().toastMessgae(DELETED)
            }.addOnFailureListener {
                requireActivity().toastMessgae(ERROR_MSG)
            }
        } catch (e: Exception) {
            Log.e(ERROR, ERROR_MSG)
        }
    }

}