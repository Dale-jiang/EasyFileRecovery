package com.file.easyfilerecovery.ui.recover

import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.file.easyfilerecovery.R
import com.file.easyfilerecovery.data.FileInfo
import com.file.easyfilerecovery.data.RecoverType
import com.file.easyfilerecovery.data.StorageType
import com.file.easyfilerecovery.databinding.ActivityFileRecoverListBinding
import com.file.easyfilerecovery.ui.base.BaseActivity
import com.file.easyfilerecovery.ui.common.GlobalViewModel.Companion.allRecoverableFiles
import com.file.easyfilerecovery.utils.CommonUtils.getPastTimeRange
import com.file.easyfilerecovery.utils.FileUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                "1 ${getString(R.string.str_month)}", "3 ${getString(R.string.str_month)}", "6 ${getString(R.string.str_month)}", "12 ${getString(R.string.str_month)}"
            )
        )
    }

    private val defaultSelections: MutableMap<RecoverType, BooleanArray> by lazy {
        filterOptions.mapValues { BooleanArray(it.value.size) { true } }.toMutableMap()
    }

    private val recoverType by lazy { intent?.getSerializableExtra(RECOVER_TYPE_KEY) as? RecoverType }
    private var currentTabIndex = 0
    private var tabMediator: TabLayoutMediator? = null

    override fun initUI() {
        binding.tvTitle.text = recoverType?.getRecoverName(this) ?: ""
        initViewPagerAndTabs()
    }


    private fun initViewPagerAndTabs() {
        lifecycleScope.launch(Dispatchers.IO) {
            val allFiles = allRecoverableFiles.toMutableList().onEach { it.checked = false }
            val filtered = filterFilesBySelection(recoverType, allFiles)

            val storageTypes = listOf(
                StorageType.HIDDEN, StorageType.STORAGE
            ) + if (recoverType == RecoverType.PHOTO || recoverType == RecoverType.VIDEO) {
                listOf(StorageType.ALBUM)
            } else emptyList()

            val tabInfo = storageTypes.mapNotNull { storageType ->
                val count = filtered.count { it.storageType == storageType }
                if (count >= 0) {
                    getTabTitle(storageType, count) to FileListFragment.newInstance(recoverType!!, storageType)
                } else null
            }

            withContext(Dispatchers.Main) {
                bindViewPagerAndTabs(tabInfo)
                setBtnStatus()
            }
        }
    }

    private fun getTabTitle(type: StorageType, count: Int): String {
        val titleRes = when (type) {
            StorageType.HIDDEN -> R.string.str_hidden
            StorageType.STORAGE -> R.string.str_storage
            StorageType.ALBUM -> R.string.str_album
        }
        return "${getString(titleRes)}($count)"
    }

    private fun bindViewPagerAndTabs(tabInfo: List<Pair<String, Fragment>>) {
        currentTabIndex = binding.tabLayout.selectedTabPosition
        tabMediator?.detach()

        binding.viewPager.apply {
            offscreenPageLimit = tabInfo.size
            adapter = object : FragmentStateAdapter(this@FileRecoveryListActivity) {
                override fun getItemCount() = tabInfo.size
                override fun createFragment(position: Int) = tabInfo[position].second
            }
        }

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = tabInfo[pos].first
        }.apply { attach() }

        binding.tabLayout.run {
            setScrollPosition(currentTabIndex, 0f, false)
            getTabAt(currentTabIndex)?.select()
        }
    }


    fun filterFilesBySelection(type: RecoverType?, list: List<FileInfo>): List<FileInfo> = when (type) {
        RecoverType.VIDEO -> {
            filterByRanges(
                list, defaultSelections[type] ?: BooleanArray(0), listOf(
                    0L to 3 * 60_000L, 3 * 60_000L to 10 * 60_000L, 10 * 60_000L to 20 * 60_000L, 20 * 60_000L to Long.MAX_VALUE
                )
            ) {
                it.duration
            }
        }

        RecoverType.AUDIO -> {
            filterByRanges(
                list, defaultSelections[type] ?: BooleanArray(0), listOf(
                    0L to 2 * 60_000L, 2 * 60_000L to 5 * 60_000L, 5 * 60_000L to 10 * 60_000L, 10 * 60_000L to Long.MAX_VALUE
                )
            ) {
                it.duration
            }
        }

        RecoverType.DOC -> {
            val now = System.currentTimeMillis()
            filterByRanges(
                list, defaultSelections[type] ?: BooleanArray(0), listOf(
                    getPastTimeRange(1) to now, getPastTimeRange(3) to now, getPastTimeRange(6) to now, 0L to now
                )
            ) { it.lastModified }
        }

        RecoverType.PHOTO, null -> {
            val mimeSelections = defaultSelections[type]?.withIndex()?.filter { it.value }?.mapNotNull { FileUtils.imageMimeTypes.getOrNull(it.index) } ?: emptyList()
            list.filter { it.mimeType in mimeSelections }
        }
    }


    private fun <T> filterByRanges(
        list: List<T>, selections: BooleanArray, ranges: List<Pair<Long, Long>>, keySelector: (T) -> Long
    ): List<T> {
        val chosen = selections.withIndex().filter { it.value }.map { ranges[it.index] }
        if (chosen.isEmpty()) return emptyList()

        return list.filter { item ->
            val v = keySelector(item)
            chosen.any { (start, end) -> v in start until end }
        }
    }


    override fun initListeners() {

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnFilter.setOnClickListener {
            showFilterDialog {
                initViewPagerAndTabs()
            }
        }

    }

    fun setBtnStatus() {
        val checkedList = allRecoverableFiles.filter { it.checked }
        binding.btnRecover.apply {
            isEnabled = checkedList.isNotEmpty()
            text = if (checkedList.isEmpty()) getString(R.string.str_recover) else "${getString(R.string.str_recover)} (${checkedList.size})"
        }
    }


    private fun showFilterDialog(onAction: () -> Unit) {
        val type = recoverType ?: return
        val options = filterOptions[type] ?: return
        val current = defaultSelections.getValue(type).copyOf()
        MaterialAlertDialogBuilder(this).setTitle(R.string.str_filter_files).setMultiChoiceItems(options.toTypedArray(), current) { _, which, isChecked ->
            current[which] = isChecked
        }.setPositiveButton(R.string.str_ok) { _, _ ->
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

