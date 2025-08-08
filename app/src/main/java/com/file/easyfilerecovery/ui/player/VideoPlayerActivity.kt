package com.file.easyfilerecovery.ui.player

import android.content.ComponentName
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.blankj.utilcode.util.ToastUtils
import com.file.easyfilerecovery.R
import com.file.easyfilerecovery.data.FileInfo
import com.file.easyfilerecovery.databinding.ActivityVideoPlayerBinding
import com.file.easyfilerecovery.ui.base.BaseActivity
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class VideoPlayerActivity : BaseActivity<ActivityVideoPlayerBinding>(ActivityVideoPlayerBinding::inflate) {

    companion object {
        const val RECOVER_FILE_INFO_KEY = "recover_file_info_key"
    }

    private val fileInfo by lazy { intent?.getParcelableExtra<FileInfo>(RECOVER_FILE_INFO_KEY) }

    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    override fun initUI() {
        fileInfo ?: run { finish(); return }
        initController()
    }

    @OptIn(UnstableApi::class)
    private fun initController() {

        val token = SessionToken(this, ComponentName(this, VideoPlayerService::class.java))

        controllerFuture = MediaController.Builder(this, token).buildAsync()

        lifecycleScope.launch {
            val controller = controllerFuture.await()
            mediaController = controller
            binding.playerView.player = controller
            binding.playerView.hideController()

            fileInfo?.let { info ->
                val mediaItem = MediaItem.Builder()
                    .setUri(info.filePath.toUri())
                    .setMediaMetadata(MediaMetadata.Builder().setTitle(info.fileName).build())
                    .build()
                controller.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        ToastUtils.showShort(getString(R.string.str_unknown_error))
                    }
                })
                controller.setMediaItem(mediaItem)
                controller.prepare()
                controller.play()
            }
        }
    }

    private fun edgeToEdge() {
        runCatching {
            enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
                val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                binding.root.setPadding(0, systemBarsInsets.top, 0, systemBarsInsets.bottom)
                insets
            }
        }.onFailure { throwable ->
            throwable.printStackTrace()
        }
    }

    override fun onAttachedToWindow() {
        edgeToEdge()
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaController?.run {
            stop()
            release()
        }
        mediaController = null
    }
}

