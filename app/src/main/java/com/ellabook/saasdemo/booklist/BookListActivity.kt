package com.ellabook.saasdemo.booklist

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ellabook.saasdemo.BaseActivity
import com.ellabook.saasdemo.R
import com.ellabook.saasdemo.booklist.fragment.BookHistoryListFragment
import com.ellabook.saasdemo.booklist.fragment.BookListFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * created by dongdaqing 11/15/21 3:00 PM
 */
class BookListActivity : BaseActivity() {

    private lateinit var bookViewModel: BookHistoryListViewModel

    private lateinit var tab: TabLayout
    private lateinit var viewpager: ViewPager2

    private val fragments: MutableList<Fragment> = ArrayList()
    private val tabString: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bookViewModel = ViewModelProvider(this).get(BookHistoryListViewModel::class.java)

        tab = findViewById(R.id.tab)
        viewpager = findViewById(R.id.viewpager)

        var fragment: Fragment
        fragment = BookListFragment()
        fragments.add(fragment)
        fragment = BookHistoryListFragment()
        fragments.add(fragment)

        tabString.add("全部图书")
        tabString.add("历史搜索")

        viewpager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }

        }

        TabLayoutMediator(tab, viewpager) { tab, position ->
            tab.text = tabString[position]
        }.attach()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }
}