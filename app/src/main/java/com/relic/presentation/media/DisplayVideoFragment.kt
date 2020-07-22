package com.relic.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.relic.R
import kotlinx.android.synthetic.main.display_video.*
import java.io.IOException

private const val KEY_CURRENT_POSITION = "KEY_CURRENT_POSITION"

class DisplayVideoFragment : DialogFragment() {
    private val args: DisplayVideoFragmentArgs by navArgs()
    private val url by lazy { args.url }
    private val audioUrl by lazy { args.audioUrl }

    private lateinit var player: SimpleExoPlayer
    private var position: Long? = null

    override fun getTheme(): Int = R.style.FullScreenDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            position = it.getLong(KEY_CURRENT_POSITION)
        }
        player = SimpleExoPlayer.Builder(requireContext()).build()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.display_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerView.player = player.apply {
            playWhenReady = true
        }

        val dataSourceFactory = DefaultDataSourceFactory(requireContext(), "no-user-agent")
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(url.toUri())

        player.addListener(object : Player.EventListener {
            // only retry once
            var hasFailed = false

            override fun onLoadingChanged(isLoading: Boolean) {
                if (!isLoading) loadingView.visibility = View.GONE
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                    val cause = error.cause
                    if (!hasFailed && cause is HttpDataSource.InvalidResponseCodeException && cause.responseCode == 403) {
                        hasFailed = true
                        // some videos may not have audio, so loading a source will fail
                        player.prepare(mediaSource)
                        position?.let { player.seekTo(it) }
                    }
                }
            }
        })

        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioUrl.toUri())
        player.prepare(MergingMediaSource(mediaSource, audioSource))

        position?.let { player.seekTo(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_CURRENT_POSITION, player.currentPosition)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
