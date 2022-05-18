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
import com.example.library.data.adapter.FirebaseAdapter
import com.example.library.databinding.FragmentBooksBinding
import com.example.mylibrary.data.adapter.BookAdapter
import com.example.mylibrary.data.model.Book
import com.example.mylibrary.utils.Constant.ERROR_MSG
import com.example.mylibrary.utils.toastMessgae
import com.google.firebase.database.*
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_books.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BooksFragment : Fragment(R.layout.fragment_books) , FirebaseAdapter.OnClickItem {

    private var _binding: FragmentBooksBinding? = null
    private val binding get() = _binding!!
    private lateinit var mNavController: NavController
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNavController = findNavController()
        database = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://my-library-d6912-default-rtdb.firebaseio.com/Books")
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

        loadDataInRecyclerView()

        return binding.root
    }

    private fun loadDataInRecyclerView() {
        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list:ArrayList<Book> = ArrayList()
                for (data in snapshot.children){
                    var model = data.getValue(Book::class.java)
                    list.add(model!!)
                }
                val adapter = FirebaseAdapter(list,this@BooksFragment)
                binding.booksRecycler.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.booksRecycler.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                requireContext().toastMessgae(ERROR_MSG)
            }

        })
    }

    override fun onItemClick(book: Book) {
        val action = BooksFragmentDirections.actionBooksFragmentToEditBookFragment(book)
        mNavController.navigate(action)
    }

    override fun onVideoClick(book: Book) {
        val action = BooksFragmentDirections.actionBooksFragmentToVideoFragment(book)
        mNavController.navigate(action)
    }


}