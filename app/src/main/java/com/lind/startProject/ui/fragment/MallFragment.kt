package com.lind.startProject.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.lind.startProject.R
import com.lind.startProject.adapter.MallProductAdapter
import com.lind.startProject.bean.ProductBean
import com.lind.startProject.databinding.FragmentMallBinding
import com.lind.lib_base.uibase.BaseFragment
import com.lind.lib_base.uibase.viewmodel.DefaultViewModel
import com.lind.lib_service.utils.SystemBarHelper

class MallFragment : BaseFragment<DefaultViewModel, FragmentMallBinding>() {
    var mData = ArrayList<ProductBean>()
    lateinit var mAdapter: MallProductAdapter
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        mAdapter = MallProductAdapter(mData)
        mViewBinding?.productList?.layoutManager = GridLayoutManager(activity, 3)
        mViewBinding?.productList?.adapter = mAdapter
        mAdapter.setOnItemClickListener {
            val uri: Uri = Uri.parse("http://www.taobao.com")
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            intent.data = uri
            startActivity(intent)
            showToast(it.productName)
        }
    }

    override fun initData() {
        super.initData()
        mData.add(ProductBean(R.mipmap.icon_product_1, "计数单杠"))
        mData.add(ProductBean(R.mipmap.icon_product_2, "计数俯卧撑板"))
        mData.add(ProductBean(R.mipmap.icon_product_3, "计数健腹轮"))
        mData.add(ProductBean(R.mipmap.icon_product_4, "计数跳绳"))
        mData.add(ProductBean(R.mipmap.icon_product_5, "计数羽毛球拍"))
        mData.add(ProductBean(R.mipmap.icon_product_6, "家用跑步机"))
        mData.add(ProductBean(R.mipmap.icon_product_7, "腕力球"))
        mData.add(ProductBean(R.mipmap.icon_product_8, "智能计数跳绳"))
        mData.add(ProductBean(R.mipmap.icon_product_9, "智能体脂秤"))
        mAdapter.setItems(mData)
    }

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentMallBinding = FragmentMallBinding.inflate(inflater, viewGroup, false)
}