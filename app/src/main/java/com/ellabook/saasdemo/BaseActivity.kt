package com.ellabook.saasdemo

import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils

/**
 * created by dongdaqing 12/3/21 10:17 AM
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        LogUtils.d("${this.javaClass.simpleName}->onCreate")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtils.d("${this.javaClass.simpleName}->onCreate")
    }

    override fun onStart() {
        super.onStart()
        LogUtils.d("${this.javaClass.simpleName}->onStart")
    }

    override fun onRestart() {
        super.onRestart()
        LogUtils.d("${this.javaClass.simpleName}->onRestart")
    }

    override fun onResume() {
        super.onResume()
        LogUtils.d("${this.javaClass.simpleName}->onResume")
    }

    override fun onPause() {
        super.onPause()
        LogUtils.d("${this.javaClass.simpleName}->onPause")
    }

    override fun onStop() {
        super.onStop()
        LogUtils.d("${this.javaClass.simpleName}->onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtils.d("${this.javaClass.simpleName}->onDestroy")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        LogUtils.d("${this.javaClass.simpleName}->onRestoreInstanceState")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        LogUtils.d("${this.javaClass.simpleName}->onSaveInstanceState")
    }

    override fun getLastNonConfigurationInstance(): Any? {
        LogUtils.d("${this.javaClass.simpleName}->getLastNonConfigurationInstance")
        return super.getLastNonConfigurationInstance()
    }

    override fun getLastCustomNonConfigurationInstance(): Any? {
        LogUtils.d("${this.javaClass.simpleName}->getLastCustomNonConfigurationInstance")
        return super.getLastCustomNonConfigurationInstance()
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        LogUtils.d("${this.javaClass.simpleName}->onRetainCustomNonConfigurationInstance")
        return super.onRetainCustomNonConfigurationInstance()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LogUtils.d("${this.javaClass.simpleName}->onConfigurationChanged")
    }
}