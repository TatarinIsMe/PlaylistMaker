package com.example.playlistmaker.presentation.player

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AudioPlayerFragment : Fragment(R.layout.activity_audio_player) {

    private val trackId: Long by lazy {
        val args = requireArguments()
        if (!args.containsKey(ARG_TRACK_ID)) {
            error("Track ID argument is required")
        }
        args.getLong(ARG_TRACK_ID).takeIf { it >= 0 } ?: error("Track ID argument is invalid")
    }

    private val viewModel: AudioPlayerViewModel by viewModel { parametersOf(trackId) }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnPlay = view.findViewById(R.id.btnPlay)
        ivCover = view.findViewById(R.id.ivCover)
        tvTrackName = view.findViewById(R.id.tvTrackName)
        tvArtistName = view.findViewById(R.id.tvArtistName)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvProgress = view.findViewById(R.id.tvProgress)
        tvAlbumLabel = view.findViewById(R.id.tvAlbumLabel)
        tvAlbum = view.findViewById(R.id.tvAlbum)
        tvYearLabel = view.findViewById(R.id.tvYearLabel)
        tvYear = view.findViewById(R.id.tvYear)
        tvGenre = view.findViewById(R.id.tvGenre)
        tvCountry = view.findViewById(R.id.tvCountry)

        view.findViewById<ImageView>(R.id.buttonBack).setOnClickListener {
            findNavController().navigateUp()
        }
        btnPlay.setOnClickListener { viewModel.onPlayPauseClicked() }

        bindObservers()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPausePlayback()
    }

    private fun bindObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
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
        const val ARG_TRACK_ID = "trackId"
    }
}
