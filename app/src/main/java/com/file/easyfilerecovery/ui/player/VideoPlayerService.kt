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
    private val tag = "VideoPlayerService"

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onCreate() {
        super.onCreate()
        runCatching {
            LogUtils.e(tag, "VideoPlayerService onCreate")
            mediaSession = MediaSession.Builder(this,
                ExoPlayer.Builder(this).setAudioAttributes(AudioAttributes.DEFAULT, true).build().apply {
                    playWhenReady = true
                    addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            ToastUtils.showShort(getString(R.string.str_unknown_error))
                        }
                    })
                }).build()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        runCatching {
            LogUtils.e(tag, "VideoPlayerService onTaskRemoved")
            mediaSession?.player?.takeIf { !it.playWhenReady || it.mediaItemCount == 0 || it.playbackState == Player.STATE_ENDED }?.run { stopSelf() }
        }
    }

    override fun onDestroy() {
        runCatching {
            mediaSession?.run {
                player.release()
                release()
                mediaSession = null
            }
        }
        super.onDestroy()
    }
}