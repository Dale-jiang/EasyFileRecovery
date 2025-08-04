@file:Suppress("DEPRECATION")

package com.file.easyfilerecovery.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import com.file.easyfilerecovery.ui.common.MainActivity
import com.file.easyfilerecovery.ui.guide.SplashActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class AppLifeManager(
    private val application: Application,
    private val onHotStart: (Activity) -> Unit = { ctx ->
        ctx.launchActivity<SplashActivity> {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }
) : Application.ActivityLifecycleCallbacks {

    private val activities = CopyOnWriteArrayList<Activity>()

    val foregroundCount = AtomicInteger(0)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var hotStartJob: Job? = null

    @Volatile
    private var hotStartPending = false

    @Volatile
    private var navigatingToSettings = false

    fun initialize() {
        application.registerActivityLifecycleCallbacks(this)
    }

    fun setNavigatingToSettings(flag: Boolean) {
        navigatingToSettings = flag
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activities += activity
    }

    override fun onActivityStarted(activity: Activity) {
        val count = foregroundCount.incrementAndGet()
        if (count == 1) {
            hotStartJob?.cancel()
            if (hotStartPending && isScreenInteractive()) {
                hotStartPending = false
                if (navigatingToSettings) {
                    setNavigatingToSettings(false)
                    return
                }

                if (activity is SplashActivity) return

                onHotStart(activity)
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {
//        runCatching {
//            activity.setLanguageConf()
//            application.setLanguageConf()
//            IronSource.onResume(activity)
//            Adjust.onResume()
//        }
    }

    override fun onActivityPaused(activity: Activity) {
//        runCatching {
//            IronSource.onPause(activity)
//            Adjust.onPause()
//        }
    }

    override fun onActivityStopped(activity: Activity) {
        val count = foregroundCount.decrementAndGet().coerceAtLeast(0)

        if (count == 0 && !navigatingToSettings) {
            hotStartJob?.cancel()
            hotStartJob = scope.launch {
                delay(3_000L)
                hotStartPending = true
//                MainActivity.showBackInt = false
//                MainActivity.intAdShowing = false
//                MainActivity.canShowRatingDialog = false
                activities.forEach { if (it !is MainActivity) it.finish() }
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        activities -= activity
    }

    private fun isScreenInteractive(): Boolean {
        val pm = application.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isInteractive
    }
}
