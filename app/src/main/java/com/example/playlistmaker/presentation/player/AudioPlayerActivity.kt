package com.example.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import com.example.playlistmaker.R
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.playlistmaker.domain.model.Track

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var track: Track

    private var mediaPlayer: MediaPlayer? = null

    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null

    private var isPrepared = false
    private var isPlay = false

    private lateinit var btnPlay: ImageButton
    private lateinit var tvProgress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        track = intent.getSerializableExtra(EXTRA_TRACK) as? Track
            ?: throw IllegalStateException("Track extra is required")

        btnPlay = findViewById(R.id.btnPlay)
        tvProgress = findViewById(R.id.tvProgress)

        findViewById<ImageView>(R.id.buttonBack).setOnClickListener {
            finish()
        }

        bindTrackUi()
        preparePlayer()

        btnPlay.setOnClickListener { onPlayPauseClicked() }

    }

    private fun bindTrackUi() {
        val ivCover = findViewById<ImageView>(R.id.ivCover)
        val tvTrackName = findViewById<TextView>(R.id.tvTrackName)
        val tvArtistName = findViewById<TextView>(R.id.tvArtistName)
        val tvDuration = findViewById<TextView>(R.id.tvDuration)
        val tvProgress = findViewById<TextView>(R.id.tvProgress)

        val tvAlbumLabel = findViewById<TextView>(R.id.tvAlbumLabel)
        val tvAlbum = findViewById<TextView>(R.id.tvAlbum)

        val tvYearLabel = findViewById<TextView>(R.id.tvYearLabel)
        val tvYear = findViewById<TextView>(R.id.tvYear)

        val tvGenre = findViewById<TextView>(R.id.tvGenre)
        val tvCountry = findViewById<TextView>(R.id.tvCountry)

        tvTrackName.text = track.trackName
        tvArtistName.text = track.artistName

        tvDuration.text = formatTime(track.trackTimeMillis)
        tvProgress.text = formatTime(0L)

        tvGenre.text = track.primaryGenreName.orEmpty()
        tvCountry.text = track.country.orEmpty()

        val year = track.releaseDate?.take(4)
        if (year.isNullOrBlank()) {
            tvYear.visibility = View.GONE
            tvYearLabel.visibility = View.GONE
        } else {
            tvYear.visibility = View.VISIBLE
            tvYearLabel.visibility = View.VISIBLE
            tvYear.text = year
        }


        if (track.collectionName.isNullOrBlank()) {
            tvAlbum.visibility = View.GONE
            tvAlbumLabel.visibility = View.GONE
        } else {
            tvAlbum.visibility = View.VISIBLE
            tvAlbumLabel.visibility = View.VISIBLE
            tvAlbum.text = track.collectionName
        }

        val coverUrl = track.artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg")

        Glide.with(this)
            .load(coverUrl)
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .into(ivCover)
    }

    private fun preparePlayer() {
        val url = track.previewUrl
        if (url.isNullOrBlank()) {
            btnPlay.isEnabled = false
            return
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener {
                isPrepared = true
            }
            setOnCompletionListener {
                stopProgressUpdates()
                isPlay = false
                btnPlay.setImageResource(R.drawable.ic_play)
                tvProgress.text = formatTime(0L)
            }
            prepareAsync()
        }
    }

    private fun onPlayPauseClicked() {
        if (!isPrepared) return

        val player = mediaPlayer ?: return

        if (isPlay) {
            // PAUSE
            player.pause()
            isPlay = false
            btnPlay.setImageResource(R.drawable.ic_play)
            stopProgressUpdates()
        } else {
            if (player.currentPosition >= player.duration) {
                player.seekTo(0)
                tvProgress.text = formatTime(0L)
            }

            player.start()
            isPlay = true
            btnPlay.setImageResource(R.drawable.ic_pause)
            startProgressUpdates()
        }
    }
    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressRunnable = Runnable {
            val player = mediaPlayer ?: return@Runnable
            tvProgress.text = formatTime(player.currentPosition.toLong())
            handler.postDelayed(progressRunnable!!, UPDATE_PROGRESS_DELAY)
        }
        handler.postDelayed(progressRunnable!!, UPDATE_PROGRESS_DELAY)
    }

    private fun stopProgressUpdates() {
        progressRunnable?.let { handler.removeCallbacks(it) }
        progressRunnable = null
    }

    override fun onPause() {
        super.onPause()
        val player = mediaPlayer ?: return
        if (isPlay) {
            player.pause()
            isPlay = false
            btnPlay.setImageResource(R.drawable.ic_play)
            stopProgressUpdates()
        }
    }

    override fun onDestroy() {
        stopProgressUpdates()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    companion object {
        const val UPDATE_PROGRESS_DELAY = 300L
        const val EXTRA_TRACK = "extra_track"
    }
}
