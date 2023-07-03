package com.ellabook.saasdemo.booklist.fragment

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.GsonUtils
import com.ellabook.saasdemo.MyApplication
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * created by dongdaqing 11/15/21 3:02 PM
 */
class BookListViewModel : ViewModel() {

    private val books: ArrayList<Book> = ArrayList()
    var searchMode = false

    val bookList: MutableLiveData<BookList> = MutableLiveData()

    fun initData() {
        bookList.value ?: getAllData()
    }

    fun searchData(text: String?) {
        if (TextUtils.isEmpty(text)) {
            searchMode = false
            getAllData()
        } else {
            searchMode = true
            viewModelScope.launch {
                bookList.value = filterData(text!!)
            }
        }
    }

    private fun getAllData() {
        viewModelScope.launch {
            if (books.isEmpty()) {
                books.addAll(openBookListFile())
            }
            bookList.value = BookList(ArrayList(books), true)
        }
    }

    private suspend fun filterData(text: String): BookList {
        return withContext(Default) {
            val result: List<Book> = books.filter {
                it.bookName.contains(text.trim(), true) || it.bookCode.contains(text.trim(), true)
            }
            BookList(result, true)
        }
    }

    private suspend fun openBookListFile(): List<Book> {
        return withContext(IO) {
            val inputReader = InputStreamReader(MyApplication.CONTEXT.assets.open("books.json"))
            val bufReader = BufferedReader(inputReader)
            var line: String?
            val builder = StringBuilder()
            while (bufReader.readLine().also { line = it } != null) {
                builder.append(line)
            }
            inputReader.close()
            bufReader.close()

            val result: List<Book> =
                GsonUtils.fromJson(builder.toString(), GsonUtils.getListType(Book::class.java))

            result
        }
    }

    private fun release() {

    }

    override fun onCleared() {
        super.onCleared()
        release()
    }

}

data class Book(val bookCode: String, val bookName: String)

class BookList(val books: List<Book>, val returnTop: Boolean)