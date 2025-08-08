package com.file.easyfilerecovery.ui.recover

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ConvertUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.file.easyfilerecovery.R
import com.file.easyfilerecovery.data.FileInfo
import com.file.easyfilerecovery.data.RecoverType
import com.file.easyfilerecovery.databinding.ItemFileMediaBinding
import com.file.easyfilerecovery.databinding.ItemFileOtherBinding
import com.file.easyfilerecovery.databinding.ItemFileTitleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileListAdapter(
    private val recoverType: RecoverType,
    private val onChecked: () -> Unit = {},
    private val onItemClick: (item: FileInfo, imgId: Int) -> Unit = { _, _ -> }
) : ListAdapter<FileInfo, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_MEDIA = 1
        private const val TYPE_DOC = 2
        private const val PAYLOAD_CHECK = "payload_check"

        val DIFF = object : DiffUtil.ItemCallback<FileInfo>() {
            override fun areItemsTheSame(old: FileInfo, new: FileInfo) =
                old.filePath == new.filePath

            override fun areContentsTheSame(old: FileInfo, new: FileInfo) =
                old == new

            override fun getChangePayload(old: FileInfo, new: FileInfo): Any? =
                if (old.checked != new.checked) PAYLOAD_CHECK else null
        }
    }

    init {
        setHasStableIds(true)
    }

    private val rounded by lazy { RoundedCorners(ConvertUtils.dp2px(11f)) }
    private val dateFmt by lazy { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    inner class TitleHolder(val binding: ItemFileTitleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FileInfo) {
            binding.tvTitle.text = item.title
            itemView.setOnClickListener(null)
        }
    }

    inner class MediaHolder(val binding: ItemFileMediaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FileInfo, payloads: List<Any>) {
            if (payloads.contains(PAYLOAD_CHECK)) {
                binding.ivCheck.setImageResource(if (item.checked) R.drawable.ic_item_checked else R.drawable.ic_item_uncheck_grey)
                return
            }
            Glide.with(binding.ivCover)
                .load(item.filePath)
                .transform(CenterCrop(), rounded)
                .into(binding.ivCover)

            binding.ivPlay.isVisible = recoverType == RecoverType.VIDEO
            binding.ivCheck.setImageResource(if (item.checked) R.drawable.ic_item_checked else R.drawable.ic_item_uncheck_grey)

            binding.ivCheck.setOnClickListener {
                val pos = adapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return@setOnClickListener
                val cur = getItem(pos)
                submitList(currentList.toMutableList().apply { set(pos, cur.copy(checked = !cur.checked)) })
                notifyItemChanged(pos, PAYLOAD_CHECK)
                onChecked.invoke()
            }
            itemView.setOnClickListener { onItemClick.invoke(item, -1) }
        }
    }

    inner class FileHolder(val binding: ItemFileOtherBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FileInfo, payloads: List<Any>) {
            if (payloads.contains(PAYLOAD_CHECK)) {
                binding.ivCheck.setImageResource(if (item.checked) R.drawable.ic_item_checked else R.drawable.ic_item_uncheck_grey)
                return
            }
            val imageRes = mimeIcon(item.mimeType)
            binding.ivImage.setImageResource(imageRes)
            binding.tvName.text = item.fileName
            binding.itemSecond.text = dateFmt.format(Date(item.lastModified))
            binding.ivCheck.setImageResource(if (item.checked) R.drawable.ic_item_checked else R.drawable.ic_item_uncheck_grey)

            binding.ivCheck.setOnClickListener {
                val pos = adapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return@setOnClickListener
                val cur = getItem(pos)
                submitList(currentList.toMutableList().apply { set(pos, cur.copy(checked = !cur.checked)) })
                notifyItemChanged(pos, PAYLOAD_CHECK)
                onChecked.invoke()
            }
            itemView.setOnClickListener { onItemClick.invoke(item, imageRes) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when {
            item.isTitle -> TYPE_TITLE
            recoverType == RecoverType.PHOTO || recoverType == RecoverType.VIDEO -> TYPE_MEDIA
            else -> TYPE_DOC
        }
    }

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return (item.filePath.hashCode()).toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TITLE -> TitleHolder(ItemFileTitleBinding.inflate(inflater, parent, false))
            TYPE_MEDIA -> MediaHolder(ItemFileMediaBinding.inflate(inflater, parent, false))
            else -> FileHolder(ItemFileOtherBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position)
        when (holder) {
            is TitleHolder -> holder.bind(item)
            is MediaHolder -> holder.bind(item, payloads)
            is FileHolder -> holder.bind(item, payloads)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is MediaHolder) {
            Glide.with(holder.binding.ivCover).clear(holder.binding.ivCover)
        }
        super.onViewRecycled(holder)
    }

    private fun mimeIcon(mime: String?): Int {
        val m = mime.orEmpty()
        return when {
            m.startsWith("audio/") -> R.drawable.ic_audio
            m == "text/plain" -> R.drawable.ic_doc
            m == "application/pdf" -> R.drawable.ic_item_pdf
            m == "text/csv" -> R.drawable.ic_doc
            m == "application/vnd.ms-xpsdocument" -> R.drawable.ic_doc
            m == "application/vnd.ms-powerpoint" ||
                    m == "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> R.drawable.ic_item_ppt

            m == "application/vnd.ms-excel" ||
                    m == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> R.drawable.ic_item_excel

            m == "application/zip" || m == "application/vnd.rar" || m == "application/x-7z-compressed" -> R.drawable.ic_item_zip
            else -> R.drawable.ic_item_doc
        }
    }

}