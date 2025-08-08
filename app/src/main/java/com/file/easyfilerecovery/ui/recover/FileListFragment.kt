package com.file.easyfilerecovery.ui.recover

import android.annotation.SuppressLint
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


    @SuppressLint("NotifyDataSetChanged")
    private fun handleData() {
        val ctx = requireContext()
        val act = requireActivity() as FileRecoveryListActivity

        viewLifecycleOwner.lifecycleScope.launch {

            val resultList = withContext(Dispatchers.Default) {
                act.filterFilesBySelection(recoverType, allRecoverableFiles)
                    .asSequence()
                    .filter { it.storageType == storageType }
                    .groupBy { it.title }
                    .entries
                    .flatMap { (title, items) ->
                        sequenceOf(FileInfo(title = title, isTitle = true)) + items.asSequence()
                    }
                    .toList()
            }

            if (!isAdded) return@launch

            val adapter = (binding.recyclerView.adapter as? FileListAdapter) ?: run {
                FileListAdapter(
                    ctx,
                    recoverType,
                    mutableListOf(),
                    onChecked = { },
                    onItemClick = { _, _ -> }
                ).also { binding.recyclerView.adapter = it }
            }

            if (binding.recyclerView.layoutManager == null) {
                binding.recyclerView.layoutManager = when (recoverType) {
                    RecoverType.PHOTO, RecoverType.VIDEO -> {
                        GridLayoutManager(ctx, 3).apply {
                            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                                override fun getSpanSize(position: Int): Int {
                                    return if (adapter.getItemViewType(position) == 0) 3 else 1
                                }
                            }
                        }
                    }

                    else -> LinearLayoutManager(ctx)
                }
                binding.recyclerView.itemAnimator = null
            }

            adapter.list.clear()
            adapter.list.addAll(resultList)
            adapter.notifyDataSetChanged()
            binding.tvEmpty.isVisible = resultList.isEmpty()
        }
    }

}