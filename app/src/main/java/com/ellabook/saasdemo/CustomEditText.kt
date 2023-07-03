package com.ellabook.saasdemo

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText
import com.blankj.utilcode.util.LogUtils

/**
 * created by dongdaqing 11/16/21 9:59 AM
 */

private const val MESSAGE_SEARCH = 1
private const val MESSAGE_DELAY = 1500L

class CustomEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) :
    AppCompatEditText(context, attrs, defStyleAttr) {

    var isInit = false

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_SEARCH -> onTextWatchListener?.text(text?.toString())
            }
        }
    }

    var onTextWatchListener: OnTextWatchListener? = null

    init {
        setOnEditorActionListener { v, actionId, event ->
            LogUtils.d("View=$v,actionId=$actionId,event=$event")

            mHandler.removeMessages(MESSAGE_SEARCH)
            mHandler.sendEmptyMessage(MESSAGE_SEARCH)
//            onTextWatchListener?.text(v.text.toString())
            true
        }

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (isInit) {
                    mHandler.removeMessages(MESSAGE_SEARCH)
                    mHandler.sendEmptyMessageDelayed(MESSAGE_SEARCH, MESSAGE_DELAY)
                }
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInit = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeMessages(MESSAGE_SEARCH)
        isInit = false
    }
}

fun interface OnTextWatchListener {
    fun text(text: String?)
}