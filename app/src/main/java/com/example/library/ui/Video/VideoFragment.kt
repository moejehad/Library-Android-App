package com.example.library.ui.Video

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.library.R
import com.example.library.databinding.FragmentVideoBinding
import com.example.mylibrary.data.model.Book
import com.example.mylibrary.utils.Constant
import com.example.mylibrary.utils.Constant.VIDEO_URL
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class VideoFragment : Fragment(R.layout.fragment_video) {

    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!

    lateinit var exoPlayer: PlayerView
    private lateinit var simpleExoPlayerView: SimpleExoPlayer
    private lateinit var mediaSource: MediaSource
    private lateinit var urlType: UrlType

    private lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        book = arguments?.getParcelable<Book>(VIDEO_URL)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  FragmentVideoBinding.inflate(inflater, container, false)

        exoPlayer = binding.exoplayer
        initPlayer(book.bookVideo)

        return binding.root
    }

    private fun initPlayer(video: String) {
        simpleExoPlayerView = SimpleExoPlayer.Builder(requireContext()).build()
        exoPlayer.player = simpleExoPlayerView
        createMediaSource(video!!)
        simpleExoPlayerView.setMediaSource(mediaSource)
        simpleExoPlayerView.prepare()
    }

    private fun createMediaSource(url: String) {

        urlType = UrlType.MP4
        urlType.url = url

        simpleExoPlayerView.seekTo(0)
        when (urlType) {
            UrlType.MP4 -> {
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                    requireContext(),
                    Util.getUserAgent(requireContext(), requireContext().applicationInfo.name)
                )
                mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(Uri.parse(urlType.url))
                )
            }
            UrlType.HLS -> {
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                    requireContext(),
                    Util.getUserAgent(requireContext(), requireContext().applicationInfo.name)
                )
                mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(Uri.parse(urlType.url))
                )
            }
        }
    }

    private var playerListener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()

            if (urlType == UrlType.HLS) {
                exoPlayer.useController = false
            }
            if (urlType == UrlType.MP4) {
                exoPlayer.useController = true
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)

        }
    }

    override fun onResume() {
        super.onResume()
        simpleExoPlayerView.playWhenReady = true
        simpleExoPlayerView.play()
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayerView.pause()
        simpleExoPlayerView.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        simpleExoPlayerView.pause()
        simpleExoPlayerView.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()

        simpleExoPlayerView.removeListener(playerListener)
        simpleExoPlayerView.pause()
        simpleExoPlayerView.clearMediaItems()
    }
}

enum class UrlType(var url: String) {
    MP4(""), HLS("")
}
