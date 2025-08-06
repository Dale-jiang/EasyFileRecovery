package com.file.easyfilerecovery.ui.recover

import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.file.easyfilerecovery.R
import com.file.easyfilerecovery.data.RecoverType
import com.file.easyfilerecovery.databinding.ActivityFileRecoverListBinding
import com.file.easyfilerecovery.ui.base.BaseActivity
import com.file.easyfilerecovery.ui.common.GlobalViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator

@Suppress("DEPRECATION")
class FileRecoveryListActivity : BaseActivity<ActivityFileRecoverListBinding>(ActivityFileRecoverListBinding::inflate) {

    companion object {
        const val RECOVER_TYPE_KEY = "recover_type_key"
    }

    private val filterOptions: Map<RecoverType, List<String>> by lazy {
        mapOf(
            RecoverType.PHOTO to listOf("Png", "Jpg", "Gif", "Heif", "Bmp", "Webp"),
            RecoverType.VIDEO to listOf("0-3 min", "3-10 min", "10-20 min", "20+ min"),
            RecoverType.AUDIO to listOf("0-2 min", "2-5 min", "5-10 min", "10+ min"),
            RecoverType.DOC to listOf(
                "1 ${getString(R.string.str_month)}",
                "3 ${getString(R.string.str_month)}",
                "6 ${getString(R.string.str_month)}",
                "12 ${getString(R.string.str_month)}"
            )
        )
    }

    private val defaultSelections: MutableMap<RecoverType, BooleanArray> by lazy {
        filterOptions.mapValues { BooleanArray(it.value.size) { true } }.toMutableMap()
    }

    private val recoverType by lazy { intent?.getSerializableExtra(RECOVER_TYPE_KEY) as? RecoverType }
    private var currentTabIndex = 0
    private var tabMediator: TabLayoutMediator? = null

    private val globalVm: GlobalViewModel by lazy {
        ViewModelProvider(application as ViewModelStoreOwner, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[GlobalViewModel::class.java]
    }

    override fun initUI() {

        binding.apply {

            tvTitle.text = recoverType?.getRecoverName(this@FileRecoveryListActivity) ?: ""


            val tabTitles = listOf(
                "${getString(R.string.str_hidden)}(10)",
                "${getString(R.string.str_storage)}(10)",
                "${getString(R.string.str_album)}(10)"
            )

            currentTabIndex = binding.tabLayout.selectedTabPosition
            tabMediator?.detach()

            val fragments =
                listOf(FileListFragment(), FileListFragment(), FileListFragment())

            binding.viewPager.offscreenPageLimit = fragments.size
            binding.viewPager.adapter = object : FragmentStateAdapter(this@FileRecoveryListActivity) {
                override fun getItemCount(): Int = fragments.size
                override fun createFragment(position: Int) = fragments[position]
            }

            tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position -> tab.text = tabTitles.getOrNull(position) }
            tabMediator?.attach()
            tabLayout.setScrollPosition(currentTabIndex, 0f, false)
            tabLayout.getTabAt(currentTabIndex)?.select()

        }


    }

    override fun initListeners() {

        binding.apply {

            ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnFilter.setOnClickListener {
                showFilterDialog {

                }
            }

        }

    }


    private fun showFilterDialog(onAction: () -> Unit) {
        val type = recoverType ?: return
        val options = filterOptions[type] ?: return
        val current = defaultSelections.getValue(type).copyOf()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.str_filter_files)
            .setMultiChoiceItems(options.toTypedArray(), current) { _, which, isChecked ->
                current[which] = isChecked
            }
            .setPositiveButton(R.string.str_ok) { _, _ ->
                defaultSelections[type] = current
                onAction()
            }.show()
    }

    private fun edgeToEdge() {
        runCatching {
            enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
                val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                window.decorView.setPadding(0, systemBarsInsets.top, 0, 0)
                binding.btnContainer.setPadding(0, 0, 0, systemBarsInsets.bottom)
                insets
            }
        }.onFailure { throwable ->
            throwable.printStackTrace()
        }
    }

    override fun onAttachedToWindow() {
        edgeToEdge()
    }

}

