package com.ellabook.saasdemo.booklist.fragment

import android.view.KeyEvent

/**
 * created by dongdaqing 2022/4/12 2:27 下午
 */
interface FragmentKeyDownEvent {
    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean
}