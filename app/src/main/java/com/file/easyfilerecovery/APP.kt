package com.file.easyfilerecovery

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.file.easyfilerecovery.utils.AppLifeManager

class APP : Application() {

    companion object {
        lateinit var app: APP
        lateinit var lifecycleManager: AppLifeManager
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        lifecycleManager = AppLifeManager(this)
        lifecycleManager.initialize()
        LogUtils.getConfig().isLogSwitch = BuildConfig.DEBUG
    }


}