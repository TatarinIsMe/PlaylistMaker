package com.example.playlistmaker.presentation.player

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.playlistmaker.App
import com.example.playlistmaker.R

class AudioPlayerActivity : AppCompatActivity() {

    private val creator by lazy { (applicationContext as App).creator }
    private val trackId: Long by lazy {
        intent.getLongExtra(EXTRA_TRACK_ID, -1L).takeIf { it >= 0 }
            ?: throw IllegalStateException("Track ID extra is required")
    }
    private val viewModel: AudioPlayerViewModel by viewModels {
        AudioPlayerViewModelFactory(trackId, creator.playerInteractor)
    }

    private lateinit var btnPlay: ImageButton
    private lateinit var ivCover: ImageView
    private lateinit var tvTrackName: TextView
    private lateinit var tvArtistName: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvAlbumLabel: TextView
    private lateinit var tvAlbum: TextView
    private lateinit var tvYearLabel: TextView
    private lateinit var tvYear: TextView
    private lateinit var tvGenre: TextView
    private lateinit var tvCountry: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        btnPlay = findViewById(R.id.btnPlay)
        ivCover = findViewById(R.id.ivCover)
        tvTrackName = findViewById(R.id.tvTrackName)
        tvArtistName = findViewById(R.id.tvArtistName)
        tvDuration = findViewById(R.id.tvDuration)
        tvProgress = findViewById(R.id.tvProgress)
        tvAlbumLabel = findViewById(R.id.tvAlbumLabel)
        tvAlbum = findViewById(R.id.tvAlbum)
        tvYearLabel = findViewById(R.id.tvYearLabel)
        tvYear = findViewById(R.id.tvYear)
        tvGenre = findViewById(R.id.tvGenre)
        tvCountry = findViewById(R.id.tvCountry)

        findViewById<ImageView>(R.id.buttonBack).setOnClickListener { finish() }
        btnPlay.setOnClickListener { viewModel.onPlayPauseClicked() }

        bindObservers()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPausePlayback()
    }

    private fun bindObservers() {
        viewModel.state.observe(this) { state ->
            tvTrackName.text = state.trackName
            tvArtistName.text = state.artistName
            tvDuration.text = state.durationText
            tvProgress.text = state.progressText
            tvGenre.text = state.genreText
            tvCountry.text = state.countryText

            tvAlbum.text = state.albumText
            tvAlbum.visibility = if (state.isAlbumVisible) View.VISIBLE else View.GONE
            tvAlbumLabel.visibility = if (state.isAlbumVisible) View.VISIBLE else View.GONE

            tvYear.text = state.yearText
            tvYear.visibility = if (state.isYearVisible) View.VISIBLE else View.GONE
            tvYearLabel.visibility = if (state.isYearVisible) View.VISIBLE else View.GONE

            btnPlay.isEnabled = state.isPlayEnabled
            btnPlay.setImageResource(if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)

            Glide.with(this)
                .load(state.coverUrl)
                .placeholder(R.drawable.ic_placeholder_45)
                .error(R.drawable.ic_placeholder_45)
                .into(ivCover)
        }
    }

    companion object {
        const val EXTRA_TRACK_ID = "extra_track_id"
    }
}
