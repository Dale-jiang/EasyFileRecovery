package com.file.easyfilerecovery.ui.settings

import android.annotation.SuppressLint
import androidx.activity.addCallback
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.file.easyfilerecovery.APP
import com.file.easyfilerecovery.databinding.ActivitySettingsBinding
import com.file.easyfilerecovery.ui.base.BaseActivity

@SuppressLint("CustomSplashScreen")
class SettingsActivity : BaseActivity<ActivitySettingsBinding>(ActivitySettingsBinding::inflate) {

    override fun onResume() {
        super.onResume()
        APP.lifecycleManager.setNavigatingToSettings(false)
    }

    override fun initUI() {
        onBackPressedDispatcher.addCallback {
            finish()
        }
        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnPolicy.setOnClickListener {
            APP.lifecycleManager.setNavigatingToSettings(true)
            runCatching { CustomTabsIntent.Builder().build().launchUrl(this@SettingsActivity, "about:blank".toUri()) }
        }

    }

}

