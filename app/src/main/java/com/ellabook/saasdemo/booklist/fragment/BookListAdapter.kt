package com.ellabook.saasdemo.booklist.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ellabook.saasdemo.R

/**
 * created by dongdaqing 11/15/21 3:32 PM
 */
class BookListAdapter(
    private val context: Context,
    private val itemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<BookViewHolder>() {

    private val books: ArrayList<Book> = ArrayList()

    private val itemClick = View.OnClickListener { v ->
        v?.tag?.let {
            val holder = it as BookViewHolder
            itemClickListener.onClick(books[holder.bindingAdapterPosition])
        }
    }

    private val diffItem = object : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.bookCode == newItem.bookCode
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.bookCode == newItem.bookCode
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffItem)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false)

        val bookViewHolder = BookViewHolder(view)
        view.setOnClickListener(itemClick)
        view.tag = bookViewHolder
        bookViewHolder.itemView.setOnClickListener(itemClick)

        return bookViewHolder
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bookName.text = books[position].bookName
        holder.bookCode.text = books[position].bookCode
    }

    override fun getItemCount(): Int {
        return books.size
    }

    fun updateData(data: List<Book>, rv: RecyclerView?) {
        asyncListDiffer.submitList(data) {
            books.clear()
            books.addAll(data)
            rv?.scrollToPosition(0)
        }
    }
}

class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var bookName: TextView = itemView.findViewById(R.id.book_name)
    var bookCode: TextView = itemView.findViewById(R.id.book_code)
}

fun interface ItemClickListener {
    fun onClick(book: Book)
}