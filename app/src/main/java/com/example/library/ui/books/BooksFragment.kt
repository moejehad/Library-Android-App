package com.example.library.ui.books

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.library.R
import com.example.library.databinding.FragmentBooksBinding
import com.example.mylibrary.data.adapter.BookAdapter
import com.example.mylibrary.data.model.Book
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_books.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BooksFragment : Fragment(R.layout.fragment_books) {

    private var _binding: FragmentBooksBinding? = null
    private val binding get() = _binding!!
    private lateinit var mNavController: NavController

    private val booksAdapter = BookAdapter(object : BookAdapter.OnClickItem{
        override fun onClick(book: Book) {
            val action = BooksFragmentDirections.actionBooksFragmentToEditBookFragment(book)
            mNavController.navigate(action)
        }

    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNavController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBooksBinding.inflate(inflater, container, false)

        binding.floatingActionButtonBooksFragment.setOnClickListener {
            val action = BooksFragmentDirections.actionBooksFragmentToAddBookFragment()
            mNavController.navigate(action)
        }

        booksAdapter.getBooks()
        binding.booksRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.booksRecycler.adapter = booksAdapter.BookAdap

        return binding.root
    }


    override fun onStart() {
        super.onStart()
            booksAdapter.BookAdap!!.startListening()
    }

    override fun onStop() {
        super.onStop()
            booksAdapter.BookAdap!!.stopListening()
    }

}