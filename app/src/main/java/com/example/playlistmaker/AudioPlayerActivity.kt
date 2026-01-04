package com.example.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var track: Track

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        track = intent.getSerializableExtra(EXTRA_TRACK) as Track

        initViews()
        bindTrack()
    }

    private fun initViews() {
        findViewById<ImageView>(R.id.buttonBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun bindTrack() {
        val ivCover = findViewById<ImageView>(R.id.ivCover)
        val tvTrackName = findViewById<TextView>(R.id.tvTrackName)
        val tvArtistName = findViewById<TextView>(R.id.tvArtistName)
        val tvDuration = findViewById<TextView>(R.id.tvDuration)
        val tvAlbumLabel = findViewById<TextView>(R.id.tvAlbumLabel)
        val tvAlbum = findViewById<TextView>(R.id.tvAlbum)
        val tvYearLabel = findViewById<TextView>(R.id.tvYearLabel)
        val tvYear = findViewById<TextView>(R.id.tvYear)
        val tvGenre = findViewById<TextView>(R.id.tvGenre)
        val tvCountry = findViewById<TextView>(R.id.tvCountry)
        val tvProgress = findViewById<TextView>(R.id.tvProgress)

        tvTrackName.text = track.trackName
        tvArtistName.text = track.artistName

        tvDuration.text = track.getFormattedTime()
        tvProgress.text = "0:00"

        tvGenre.text = track.primaryGenreName.orEmpty()
        tvCountry.text = track.country.orEmpty()

        val year = track.getReleaseYear()
        if (year == null) {
            tvYear.visibility = View.GONE
            tvYearLabel.visibility = View.GONE
        } else {
            tvYear.text = year
        }

        if (track.collectionName.isNullOrEmpty()) {
            tvAlbum.visibility = View.GONE
            tvAlbumLabel.visibility = View.GONE
        } else {
            tvAlbum.text = track.collectionName
        }

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .into(ivCover)
    }

    companion object {
        const val EXTRA_TRACK = "extra_track"
    }
}
