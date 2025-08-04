package com.file.easyfilerecovery.ui.guide

import android.annotation.SuppressLint
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import com.file.easyfilerecovery.databinding.ActivitySplashBinding
import com.file.easyfilerecovery.ui.base.BaseActivity
import com.file.easyfilerecovery.ui.common.MainActivity
import com.file.easyfilerecovery.utils.firstLaunchTag
import com.file.easyfilerecovery.utils.launchActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    override fun initUI() {

        if (firstLaunchTag) loading() else loading()

    }

    private fun loading() {
        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            val adReady = awaitAdsReady(timeout = 15_000L, pollInterval = 200L)
            enforceMinimumDisplayTime(startTime, minTime = 2_000L)
            handlePostLoad(adReady)
        }
    }

    private suspend fun awaitAdsReady(timeout: Long, pollInterval: Long): Boolean {
        return withTimeoutOrNull(timeout) {
//            while (!checkAds()) {
//                delay(pollInterval)
//            }
            true
        } ?: false
    }

    private suspend fun enforceMinimumDisplayTime(startTime: Long, minTime: Long) {
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed < minTime) {
            delay(minTime - elapsed)
        }
    }

    private fun handlePostLoad(adReady: Boolean) {
        if (adReady) {
            navigateNext()
        } else {
            navigateNext()
        }
    }


    override fun initData() {
        onBackPressedDispatcher.addCallback { }
    }


    private fun navigateNext() {
        if (firstLaunchTag) {
            firstLaunchTag = false
            launchActivity<MainActivity>(finish = true)
        } else {
            launchActivity<MainActivity>(finish = true)
        }
    }

}

