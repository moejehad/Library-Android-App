package com.example.library.ui.edit

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.library.R
import com.example.library.databinding.FragmentEditBookBinding
import com.example.mylibrary.data.model.Book
import com.example.mylibrary.utils.Constant.BOOK_AUTHOR
import com.example.mylibrary.utils.Constant.BOOK_DATA
import com.example.mylibrary.utils.Constant.BOOK_NAME
import com.example.mylibrary.utils.Constant.COLLECTION_NAME
import com.example.mylibrary.utils.Constant.DELETED
import com.example.mylibrary.utils.Constant.ERROR
import com.example.mylibrary.utils.Constant.ERROR_MSG
import com.example.mylibrary.utils.Constant.UPDATE
import com.example.mylibrary.utils.toastMessgae
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_add_book.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class EditBookFragment : Fragment(R.layout.fragment_edit_book), DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentEditBookBinding? = null
    private val binding get() = _binding!!
    private lateinit var mNavController: NavController
    private lateinit var db: FirebaseFirestore
    private lateinit var book: Book
    var date: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mNavController = findNavController()
        db = FirebaseFirestore.getInstance()
        book = arguments?.getParcelable<Book>(BOOK_DATA)!!

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

        binding.LaunchYear.setOnClickListener {
            pickDate()
        }

        binding.EditBookBtn.setOnClickListener {
            GlobalScope.launch {
                EditBookFromFirestore()
            }
        }

        binding.DeleteBookBtn.setOnClickListener {
            GlobalScope.launch {
                DeleteBookFromFirestore()
            }
        }

        return binding.root
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

    private fun EditBookFromFirestore() {
        var editBookName = editTextTextBookName.text
        var editBookAuthor = editTextTextBookAuthor.text
        var editBookYear = LaunchYear.text
        var editBookPrice = editTextTextPrice.text
        var editBookRating = ratingBar.rating.toString()

        if (editBookName.isNotEmpty() && editBookAuthor.isNotEmpty() && editBookYear.isNotEmpty()
            && editBookPrice.isNotEmpty() && ratingBar.rating !== 0.0.toFloat()
        ) {

            val EditBook = Book(
                editBookName.toString(),
                editBookAuthor.toString(),
                editBookPrice.toString(),
                editBookRating,
                editBookPrice.toString()
            )

            try {
                db.collection(COLLECTION_NAME)
                    .whereEqualTo(BOOK_NAME, book.bookName)
                    .whereEqualTo(BOOK_AUTHOR, book.bookAuthor)
                    .get().addOnSuccessListener { it ->
                        db.collection(COLLECTION_NAME).document(it.documents[0].id).set(EditBook)
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

            db.collection(COLLECTION_NAME)
                .whereEqualTo(BOOK_NAME, book.bookName)
                .whereEqualTo(BOOK_AUTHOR, book.bookAuthor)
                .get().addOnSuccessListener { it ->
                    db.collection(COLLECTION_NAME).document(it.documents[0].id).delete()
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