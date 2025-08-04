package com.file.easyfilerecovery.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.file.easyfilerecovery.utils.edgeToEdge

abstract class BaseActivity<VB : ViewBinding>(private val inflate: (layoutInflater: LayoutInflater) -> VB) : AppCompatActivity() {

    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adjustSize()
        binding = inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        initListeners()
        initData()
    }

    override fun onAttachedToWindow() = edgeToEdge()

    protected abstract fun initUI()
    protected open fun initData() = Unit
    protected open fun initListeners() = Unit

    @Suppress("DEPRECATION")
    private fun adjustSize() = runCatching {
        resources.displayMetrics.run {
            density = heightPixels / 760f
            densityDpi = (density * 160).toInt()
            scaledDensity = density
        }
    }

//    override fun attachBaseContext(newBase: Context?) {
//        super.attachBaseContext(newBase?.setLanguageConf())
//    }


}