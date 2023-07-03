package com.ellabook.saasdemo.booklist

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPStaticUtils
import com.ellabook.saasdemo.booklist.fragment.Book
import com.ellabook.saasdemo.booklist.fragment.BookList
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * created by dongdaqing 11/15/21 3:02 PM
 */
const val SP_HISTORY_BOOK_LIST = "SP_HISTORY_BOOK_LIST"

class BookHistoryListViewModel : ViewModel() {

    private val MAX_BOOK_SIZE = 100

    private var bookHistory: ArrayList<Book> = ArrayList()

    private var searchMode = false

    val bookHistoryList: MutableLiveData<BookList> = MutableLiveData()

    fun initHistoryData() {
        bookHistoryList.value ?: getAllData()
    }

    fun searchData(text: String?) {
        if (TextUtils.isEmpty(text)) {
            searchMode = false
            getAllData()
        } else {
            searchMode = true
            viewModelScope.launch {
                bookHistoryList.value = filterData(text!!)
            }
        }
    }

    fun updateHistory(book: Book) {
        viewModelScope.launch {
            withContext(IO) {
                if (bookHistory.isEmpty()) {
                    openBookListHistory()?.let {
                        bookHistory.addAll(it)
                    }
                }
                bookHistory.remove(book)
                bookHistory.add(0, book)
                if (bookHistory.size > MAX_BOOK_SIZE) {
                    bookHistory.removeAt(bookHistory.size - 1)
                }

                SPStaticUtils.put(SP_HISTORY_BOOK_LIST, GsonUtils.toJson(bookHistory))
            }
            if (!searchMode) {
                bookHistoryList.value = BookList(ArrayList(bookHistory), false)
            }
        }
    }

    private fun getAllData() {
        viewModelScope.launch {
            if (bookHistory.isEmpty()) {
                openBookListHistory()?.let {
                    bookHistory.addAll(it)
                    bookHistoryList.value = BookList(ArrayList(bookHistory), true)
                }
            } else {
                bookHistoryList.value = BookList(ArrayList(bookHistory), true)
            }
        }
    }

    private suspend fun filterData(text: String): BookList {
        return withContext(Default) {
            val result: List<Book> = bookHistory.filter {
                it.bookName.contains(text.trim(), true) ||
                        it.bookCode.contains(text.trim(), true)
            }
            BookList(result, true)
        }
    }

    private suspend fun openBookListHistory(): List<Book>? {
        return withContext(IO) {

            val historyList = GsonUtils.fromJson<List<Book>>(
                SPStaticUtils.getString(SP_HISTORY_BOOK_LIST),
                GsonUtils.getListType(Book::class.java)
            )

            historyList
        }
    }

    private fun release() {

    }

    override fun onCleared() {
        super.onCleared()
        release()
    }

}