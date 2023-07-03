package com.ellabook.saasdemo.booklist.fragment

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ellabook.saasdemo.CustomEditText
import com.ellabook.saasdemo.MainActivity
import com.ellabook.saasdemo.OnTextWatchListener
import com.ellabook.saasdemo.R
import com.ellabook.saasdemo.booklist.BookHistoryListViewModel

/**
 * created by dongdaqing 2022/4/12 11:44 上午
 */
class BookListFragment : Fragment(), FragmentKeyDownEvent {

    private lateinit var bookListViewModel: BookListViewModel
    private lateinit var bookHistoryListViewModel: BookHistoryListViewModel

    private lateinit var adapter: BookListAdapter
    private lateinit var bookListView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initList(view)

        //获取/观察数据
        bookListViewModel = ViewModelProvider(this).get(BookListViewModel::class.java)
        bookHistoryListViewModel = ViewModelProvider(requireActivity()).get(BookHistoryListViewModel::class.java)

        bookListViewModel.bookList.observe(requireActivity()) {
            adapter.updateData(it.books, if (it.returnTop) bookListView else null)
        }
        bookListViewModel.initData()

        view.findViewById<CustomEditText>(R.id.search).onTextWatchListener = OnTextWatchListener {
            bookListViewModel.searchData(it)
        }
    }

    private fun initList(view: View) {
        bookListView = view.findViewById(R.id.book_list)
        //初始化列表
        val layoutManager = LinearLayoutManager(requireContext())
        bookListView.layoutManager = layoutManager
        adapter = BookListAdapter(requireContext()) {
            bookHistoryListViewModel.updateHistory(it)

            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("bookName", it.bookName)
            intent.putExtra("bookCode", it.bookCode)
            startActivity(intent)
        }
        bookListView.adapter = adapter
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }
}