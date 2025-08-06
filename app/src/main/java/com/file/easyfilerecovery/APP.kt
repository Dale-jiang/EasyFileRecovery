package com.file.easyfilerecovery

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.blankj.utilcode.util.LogUtils
import com.file.easyfilerecovery.utils.AppLifeManager

class APP : Application(), ViewModelStoreOwner {

    private lateinit var appViewModelStore: ViewModelStore

    companion object {
        lateinit var app: APP
        lateinit var lifecycleManager: AppLifeManager
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        appViewModelStore = ViewModelStore()
        lifecycleManager = AppLifeManager(this)
        lifecycleManager.initialize()
        LogUtils.getConfig().isLogSwitch = BuildConfig.DEBUG
    }

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore


}