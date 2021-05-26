package com.lind.agedbuy.ui.fragment

import android.content.*
import android.os.*
import android.service.autofill.UserData
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.LogUtils
import com.css.wondercorefit.viewmodel.MainViewModel
import com.lind.lib_base.uibase.BaseFragment
import com.lind.lib_service.utils.SystemBarHelper


class MainFragment : BaseFragment<MainViewModel, FragmentMainBinding>(), View.OnClickListener {
    private val TAG = "MainFragment"

    private lateinit var iSportStepInterface: ISportStepInterface
    private var stepArray: Int = 0
    private val mDelayHandler = Handler(TodayStepCounterCall())
    private val REFRESH_STEP_WHAT = 0
    private val TIME_INTERVAL_REFRESH: Long = 1000
    private lateinit var targetStep: String
    private var currentStep: Int = 0
    private var result: Float = 0.0f
    private lateinit var stepData: StepData
    private lateinit var mUserData: UserData
    private var needNotify: Boolean = false
    private var pauseResume: Boolean = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        showDevice()
        startSensorService()
        startStep()
        initClickListenr()
        initProgressRate()
        if (mUserData.isFirst) {
            activity?.let { PersonInformationActivity.starActivity(it) }
            mUserData.isFirst = false
            WonderCoreCache.saveUserInfo(mUserData)
        }
    }

    override fun initData() {
        super.initData()
        mViewBinding?.tvTargetWeightNum?.text = WonderCoreCache.getUserInfo().targetWeight

        WeightBondData.firstWeightInfoObsvr.let {
            it.observe(this) { it2 ->
                if (it2 != null) {
                    mViewBinding?.tvInitialWeightNum?.text = it2.weightKgFmt
                } else {
                    mViewBinding?.tvInitialWeightNum?.text = "--"
                }
            }
        }

        WeightBondData.lastWeightInfoObsvr.let {
            it.observe(this) { it2 ->
                if (it2 != null) {
                    mViewBinding?.tvCurrentWeight?.text = it2.weightKgFmt("%.1f")
                    if (it2?.getBodyFatData()?.bmi != null) {
                        mViewBinding?.llBmi?.visibility = View.VISIBLE
                        mViewBinding?.tvBmi?.text = "BMI${it2?.getBodyFatData()?.bmi}"
                        mViewBinding?.tvBodyType?.text = it2.fatLevel
                    } else {
                        mViewBinding?.llBmi?.visibility = View.GONE
                    }
                }
            }
        }

        WeightBondData.lastWeightInfo.let {
            if (it?.getBodyFatData()?.bmi != null) {
                mViewBinding?.llBmi?.visibility = View.VISIBLE
                mViewBinding?.tvBmi?.text = "BMI${it?.getBodyFatData()?.bmi}"
                mViewBinding?.tvBodyType?.text = it.fatLevel
            }
        }
    }

    private fun showDevice() {
        //是否已绑定体脂秤
        if (WonderCoreCache.getData(
                WonderCoreCache.BOND_WEIGHT_INFO,
                BondDeviceData::class.java
            ).mac.isNotEmpty()
        ) {
            mViewBinding?.llDevice?.visibility = View.VISIBLE
            mViewBinding?.deviceWeight?.visibility = View.VISIBLE
            mViewBinding?.llCurrentWeight?.visibility = View.VISIBLE
            mViewBinding?.gotoMeasure?.visibility = View.GONE
            mViewBinding?.tvNoneWeight?.visibility = View.GONE
        } else {
            mViewBinding?.gotoMeasure?.visibility = View.VISIBLE
            mViewBinding?.tvNoneWeight?.visibility = View.VISIBLE
            mViewBinding?.llCurrentWeight?.visibility = View.GONE
        }
        //是否已绑定健腹轮
        if (WonderCoreCache.getData(
                WonderCoreCache.BOND_WHEEL_INFO,
                BondDeviceData::class.java
            ).mac.isNotEmpty()
        ) {
            mViewBinding?.llDevice?.visibility = View.VISIBLE
            mViewBinding?.deviceWheel?.visibility = View.VISIBLE
        }
        //绑定监听
        sp?.registerOnSharedPreferenceChangeListener(spLis)
    }

    private val sp by lazy { activity?.getSharedPreferences("spUtils", Context.MODE_PRIVATE) }
    private val spLis by lazy {
        SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            when (key) {
                WonderCoreCache.BOND_WEIGHT_INFO -> {
                    if (WonderCoreCache.getData(
                            WonderCoreCache.BOND_WEIGHT_INFO,
                            BondDeviceData::class.java
                        ).mac.isNotEmpty()
                    ) {
                        mViewBinding?.llDevice?.visibility = View.VISIBLE
                        mViewBinding?.deviceWeight?.visibility = View.VISIBLE
                        mViewBinding?.gotoMeasure?.visibility = View.GONE
                        mViewBinding?.tvNoneWeight?.visibility = View.GONE
                        mViewBinding?.llCurrentWeight?.visibility = View.VISIBLE
                    } else {
                        mViewBinding?.llDevice?.visibility = View.GONE
                        mViewBinding?.gotoMeasure?.visibility = View.VISIBLE
                        mViewBinding?.tvNoneWeight?.visibility = View.VISIBLE
                        mViewBinding?.llCurrentWeight?.visibility = View.GONE
                    }

                    if (WonderCoreCache.getData(
                            WonderCoreCache.BOND_WHEEL_INFO,
                            BondDeviceData::class.java
                        ).mac.isNotEmpty()
                    ) {
                        mViewBinding?.llDevice?.visibility = View.VISIBLE
                        mViewBinding?.deviceWheel?.visibility = View.VISIBLE
                    } else {
                        mViewBinding?.deviceWheel?.visibility = View.INVISIBLE
                    }
                }
                WonderCoreCache.USER_INFO -> {
                    mUserData = WonderCoreCache.getUserInfo()
                    mViewBinding?.tvTargetWeightNum?.text =
                        mUserData.targetWeight
                    mViewBinding?.tvTodayStepTarget?.text =
                        "目标 ${mUserData.targetStep}"
                }

            }
            LogUtils.vTag("suisui", key)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sp?.unregisterOnSharedPreferenceChangeListener(spLis)
    }

    private fun initProgressRate() {
        mUserData = WonderCoreCache.getUserInfo()
        targetStep = mUserData.targetStep
        stepData = WonderCoreCache.getData(WonderCoreCache.STEP_DATA, StepData::class.java)
        currentStep = stepData.todaySteps
        result = ((currentStep * 100) / targetStep.toInt()).toFloat()
        Log.d(TAG, "ProgressInformation   :  $currentStep    $targetStep    $result")
        mViewBinding?.pbStep?.setProgress(result)
    }

    private fun initClickListenr() {
        mViewBinding!!.gotoMeasure.setOnClickListener(this)
        mViewBinding!!.deviceWeight.setOnClickListener(this)
        mViewBinding!!.addBleDevice.setOnClickListener(this)
    }

    private fun startSensorService() {
        val intentSensor = Intent(activity, SensorService::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            activity?.startForegroundService(intentSensor)
        } else {
            activity?.startService(intentSensor)
        }
    }

    private fun startStep() {
        activity?.let { TodayStepManager().init(it.application) }

        //开启计步Service，同时绑定Activity进行aidl通信
        val intentSteps = Intent(activity, TodayStepService::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            activity?.startForegroundService(intentSteps)
        } else {
            activity?.startService(intentSteps)
        }
        activity?.bindService(intentSteps, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                //Activity和Service通过aidl进行通信
                iSportStepInterface = ISportStepInterface.Stub.asInterface(service)
                try {
                    stepArray = iSportStepInterface.todaySportStepArray
                    updataValues(stepArray)
                    Log.d(TAG, " getTodaySportStepArray  :   $stepArray")
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
                mDelayHandler.sendEmptyMessageDelayed(
                    REFRESH_STEP_WHAT,
                    TIME_INTERVAL_REFRESH
                )
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }, AppCompatActivity.BIND_AUTO_CREATE)
    }

    private fun updataValues(stepArray: Int) {
        val realSteps = stepArray
        if (realSteps == 0) {
            mViewBinding?.tvStepNum?.text = "--"
        } else {
            mViewBinding?.tvStepNum?.text = realSteps.toString()
        }
        mViewBinding?.tvWalkingDistanceNum?.text = getDistanceByStep(realSteps.toLong())
        mViewBinding?.tvCalorieConsumptionNum?.text = getCalorieByStep(realSteps.toLong())
        result = ((realSteps * 100) / targetStep.toInt()).toFloat()
        mViewBinding?.pbStep?.setProgress(result)
    }

    // 公里计算公式
    private fun getDistanceByStep(steps: Long): String {
        return String.format("%.2f", steps * 0.6f / 1000)
    }

    // 千卡路里计算公式
    private fun getCalorieByStep(steps: Long): String {
        return String.format("%.1f", steps * 0.6f * 60 * 1.036f / 1000)
    }

    inner class TodayStepCounterCall : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                REFRESH_STEP_WHAT -> {

                    //每隔500毫秒获取一次计步数据刷新UI
                    if (null != iSportStepInterface) {
                        try {
                            stepArray = iSportStepInterface.todaySportStepArray
                            updataValues(stepArray)
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        }
                        if (stepArray != stepArray) {
                            stepArray = stepArray
                            updataValues(stepArray)
                        }
                    }
                    mDelayHandler.sendEmptyMessageDelayed(
                        REFRESH_STEP_WHAT,
                        TIME_INTERVAL_REFRESH
                    )
                }
            }
            return false
        }
    }

    override fun initViewModel(): MainViewModel =
        ViewModelProvider(this).get(MainViewModel::class.java)


    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentMainBinding = FragmentMainBinding.inflate(inflater, viewGroup, false)

    override fun onClick(v: View) {
        when (v.id) {
            R.id.device_weight -> {
                ARouter.getInstance()
                    .build(ARouterConst.PATH_APP_BLE_WEIGHTMEASURE)
                    .navigation()
            }
            R.id.goto_measure -> {
                activity?.let { ToastDialog(it).showPopupWindow() }
            }

            R.id.add_ble_device -> {
                ARouter.getInstance()
                    .build(ARouterConst.PATH_APP_BLE)
                    .navigation()
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        initProgressRate()
        needNotify = if (!needNotify) {
            mDelayHandler.removeCallbacksAndMessages(null)
            true
        } else {
            mDelayHandler.removeCallbacksAndMessages(null)
            mDelayHandler.sendEmptyMessageDelayed(
                REFRESH_STEP_WHAT,
                TIME_INTERVAL_REFRESH
            )
            false
        }
    }

    override fun onPause() {
        super.onPause()
        if (!pauseResume) {
            mDelayHandler.removeCallbacksAndMessages(null)
            pauseResume = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (pauseResume) {
            mDelayHandler.sendEmptyMessageDelayed(
                REFRESH_STEP_WHAT,
                TIME_INTERVAL_REFRESH
            )
            pauseResume = false
        }
    }


}