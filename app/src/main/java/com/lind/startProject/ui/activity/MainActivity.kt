package com.lind.startProject.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lind.startProject.BuildConfig
import com.lind.startProject.R
import com.lind.startProject.databinding.ActivityMainBinding
import com.lind.startProject.ui.fragment.CourseFragment
import com.lind.startProject.ui.fragment.MainFragment
import com.lind.startProject.ui.fragment.MallFragment
import com.lind.startProject.ui.fragment.SettingFragment
import com.lind.lib_base.uibase.base.AgedBaseActivity
import com.lind.lib_base.uibase.viewmodel.DefaultViewModel
import com.lind.lib_service.inner.BaseInner
import com.tencent.bugly.Bugly

class MainActivity : AgedBaseActivity<DefaultViewModel, ActivityMainBinding>() {
    private var mCurFragment: Fragment? = null
    private lateinit var mTabMainFragment: MainFragment
    lateinit var mTabCourseFragment: CourseFragment
    private lateinit var mTabMallFragment: MallFragment
    private lateinit var mTabSettingFragment: SettingFragment

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (BuildConfig.DEBUG) {
//            Bugly.init(applicationContext, "718b817297", true)
        } else {
//            Bugly.init(applicationContext, "718b817297", false)
        }
        initTablayout()
    }

    private fun initTablayout() {
            mTabMainFragment = MainFragment()
            mTabCourseFragment = CourseFragment()
            mTabMallFragment = MallFragment()
            mTabSettingFragment = SettingFragment()
            mViewBinding.tablayout.initTab(callback = {
                mViewBinding.tablayout.tag = it
//            val fragment = getFragment(it)
//            when (it) {
//                BaseInner.TabIndex.HOME -> {
//                    if (mCurFragment == fragment) {
//                        mTabMainFragment.scrollTopRefresh()
//                    }
//
//                }
//                BaseInner.TabIndex.MALL -> {
//                    if (curFragment == fragment) {
//                        tabMallFragment.scrollTopRefresh()
//                    }
//
//                }
//                BaseInner.TabIndex.COURSE -> {
//                    if (curFragment == fragment) {
//                        tabCartFragment.scrollTopRefresh()
//                    }
//
//                }
//                BaseInner.TabIndex.SETTING -> {
//                    if (curFragment == fragment) {
//                        tabMyFragment.scrollTopRefresh()
//                    }
//
//                }
//            }
                changeFragment(it)
            })
    }
    private fun changeFragment(tabIndex: Int) {
        val fragment = getFragment(tabIndex) ?: return
        if (mCurFragment == fragment) {
            return
        }
        val newFragmentTag = fragment::class.java.simpleName
        val fragmentManager = supportFragmentManager
        val ft = fragmentManager.beginTransaction()
        if (mCurFragment != null && !mCurFragment!!.isHidden) {
            ft.hide(mCurFragment!!)
        }
        val fragmentByTag = fragmentManager.findFragmentByTag(newFragmentTag)
        if (fragmentByTag == null) {
            if (!fragment.isAdded) {
                ft.add(R.id.container_main, fragment, newFragmentTag)
            }
        } else {
            ft.show(fragmentByTag)
        }
        ft.commitAllowingStateLoss()
        mCurFragment = fragment
    }

    private fun getFragment(@BaseInner.TabIndex tabIndex: Int): Fragment? {
        when (tabIndex) {
            BaseInner.TabIndex.HOME -> return mTabMainFragment
            BaseInner.TabIndex.COURSE -> return mTabCourseFragment
            BaseInner.TabIndex.MALL -> return mTabMallFragment
            BaseInner.TabIndex.SETTING -> return mTabSettingFragment
        }
        return null
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater, parent, false)
}