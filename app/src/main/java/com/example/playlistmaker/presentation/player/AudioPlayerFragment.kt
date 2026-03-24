package com.example.playlistmaker.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityAudioPlayerBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AudioPlayerFragment : Fragment() {

    private val trackId: Long by lazy {
        val args = requireArguments()
        if (!args.containsKey(ARG_TRACK_ID)) {
            error("Track ID argument is required")
        }
        args.getLong(ARG_TRACK_ID).takeIf { it >= 0 } ?: error("Track ID argument is invalid")
    }

    private val viewModel: AudioPlayerViewModel by viewModel { parametersOf(trackId) }
    private var _binding: ActivityAudioPlayerBinding? = null
    private val binding: ActivityAudioPlayerBinding
        get() = _binding ?: error("Binding is only valid between onCreateView and onDestroyView")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityAudioPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnPlay.setOnClickListener { viewModel.onPlayPauseClicked() }
        binding.btnFavorite.setOnClickListener { viewModel.onFavoriteClicked() }

        bindObservers()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPausePlayback()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.tvTrackName.text = state.trackName
            binding.tvArtistName.text = state.artistName
            binding.tvDuration.text = state.durationText
            binding.tvProgress.text = state.progressText
            binding.tvGenre.text = state.genreText
            binding.tvCountry.text = state.countryText

            binding.tvAlbum.text = state.albumText
            binding.tvAlbum.visibility = if (state.isAlbumVisible) View.VISIBLE else View.GONE
            binding.tvAlbumLabel.visibility = if (state.isAlbumVisible) View.VISIBLE else View.GONE

            binding.tvYear.text = state.yearText
            binding.tvYear.visibility = if (state.isYearVisible) View.VISIBLE else View.GONE
            binding.tvYearLabel.visibility = if (state.isYearVisible) View.VISIBLE else View.GONE

            binding.btnPlay.isEnabled = state.isPlayEnabled
            binding.btnPlay.setImageResource(if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            binding.btnFavorite.setImageResource(
                if (state.isFavorite) R.drawable.ic_favourite_red_25 else R.drawable.ic_favourite_25
            )

            Glide.with(this)
                .load(state.coverUrl)
                .placeholder(R.drawable.ic_placeholder_45)
                .error(R.drawable.ic_placeholder_45)
                .into(binding.ivCover)
        }
    }

    companion object {
        const val ARG_TRACK_ID = "trackId"
    }
}
