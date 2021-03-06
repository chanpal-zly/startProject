package com.lind.lib_base.uibase

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.lind.lib_base.uibase.base.AgedBaseActivity
import com.lind.lib_base.uibase.viewmodel.BaseViewModel

abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : AgedBaseActivity<VM, VB>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        ActivityHolder.addActivity(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED; // 禁用横屏
    }

    override fun onStop() {
        super.onStop()
//        if (this.isFinishing) ActivityHolder.removeActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}