package com.file.easyfilerecovery.ui.recover

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.file.easyfilerecovery.data.FileInfo
import com.file.easyfilerecovery.data.RecoverType
import com.file.easyfilerecovery.data.StorageType
import com.file.easyfilerecovery.databinding.FragmentFileListBinding
import com.file.easyfilerecovery.ui.base.BaseFragment
import com.file.easyfilerecovery.ui.common.GlobalViewModel.Companion.allRecoverableFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FileListFragment : BaseFragment<FragmentFileListBinding>(FragmentFileListBinding::inflate) {

    companion object {
        private const val ARG_RECOVER_TYPE = "arg_recover_type"
        private const val ARG_STORAGE_TYPE = "arg_storage_type"

        fun newInstance(recoverType: RecoverType, storageType: StorageType): FileListFragment {
            return FileListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RECOVER_TYPE, recoverType.name)
                    putString(ARG_STORAGE_TYPE, storageType.name)
                }
            }
        }
    }

    private lateinit var recoverType: RecoverType
    private lateinit var storageType: StorageType

    override fun initUI() {

        arguments?.let {
            recoverType = RecoverType.valueOf(it.getString(ARG_RECOVER_TYPE)!!)
            storageType = StorageType.valueOf(it.getString(ARG_STORAGE_TYPE)!!)
        }
        handleData()
    }


    private fun handleData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val activity = requireActivity() as FileRecoveryListActivity
            val primaryFiltered = activity.filterFilesBySelection(recoverType, allRecoverableFiles)
            val slice = primaryFiltered.filter { it.storageType == storageType }

            val resultList = mutableListOf<FileInfo>()
            if (slice.isNotEmpty()) {
                val map = slice.groupBy { it.title }
                map.forEach {
                    resultList.add(FileInfo(title = it.value[0].title, isTitle = true))
                    resultList.addAll(it.value)
                }
            }

            withContext(Dispatchers.Main) {

                val adapter = FileListAdapter(requireContext(), recoverType, resultList, onChecked = {

                }, onItemClick = { item, imgId ->

                })

                val layoutManager = when (recoverType) {
                    RecoverType.PHOTO, RecoverType.VIDEO -> {
                        GridLayoutManager(requireContext(), 3).apply {
                            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                                override fun getSpanSize(position: Int): Int {
                                    return when (adapter.getItemViewType(position)) {
                                        0 -> 3
                                        else -> 1
                                    }
                                }
                            }
                        }
                    }

                    else -> LinearLayoutManager(requireContext())
                }
                binding.recyclerView.layoutManager = layoutManager
                binding.recyclerView.itemAnimator = null
                binding.recyclerView.adapter = adapter
                binding.tvEmpty.isVisible = resultList.isEmpty()
            }
        }

    }

}