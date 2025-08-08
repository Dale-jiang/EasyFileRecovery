package com.file.easyfilerecovery.ui.player

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.file.easyfilerecovery.R

class VideoPlayerService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onCreate() {
        super.onCreate()
        LogUtils.i(TAG, "onCreate")
        mediaSession = runCatching {
            val player = buildPlayer()
            MediaSession.Builder(this, player).build()
        }.onFailure { e ->
            LogUtils.e(TAG, "Create MediaSession failed: ${e.message}")
        }.getOrNull()
    }

    private fun buildPlayer(): ExoPlayer =
        ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true).build().apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        LogUtils.e(TAG, "Player error: ${error.message}")
                        ToastUtils.showShort(getString(R.string.str_unknown_error))
                    }
                })
            }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        LogUtils.i(TAG, "onTaskRemoved")

        val player = mediaSession?.player ?: return
        val shouldStop = !player.playWhenReady ||
                player.mediaItemCount == 0 ||
                player.playbackState == Player.STATE_ENDED

        if (shouldStop) stopSelf()
    }

    override fun onDestroy() {
        LogUtils.i(TAG, "onDestroy")
        mediaSession?.let { session ->
            session.player.release()
            session.release()
            mediaSession = null
        }
        super.onDestroy()
    }

    companion object {
        private const val TAG = "VideoPlayerService"
    }
}