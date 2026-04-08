package com.example.playlistmaker.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityAudioPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
    private val playlistsAdapter by lazy { BottomSheetPlaylistsAdapter(viewModel::onPlaylistSelected) }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                binding.overlay.visibility = View.GONE
                binding.overlay.alpha = 0f
            } else {
                binding.overlay.visibility = View.VISIBLE
                if (newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.overlay.alpha = 1f
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val alpha = ((slideOffset + 1f) / 2f).coerceIn(0f, 1f)
            binding.overlay.alpha = alpha
        }
    }

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

        setupBottomSheet()
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnAddToPlaylist.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        binding.btnPlay.setOnClickListener { viewModel.onPlayPauseClicked() }
        binding.btnFavorite.setOnClickListener { viewModel.onFavoriteClicked() }
        binding.btnNewPlaylistBottomSheet.setOnClickListener { viewModel.onNewPlaylistClicked() }
        binding.overlay.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        bindObservers()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPausePlayback()
    }

    override fun onDestroyView() {
        binding.rvBottomSheetPlaylists.adapter = null
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        _binding = null
        super.onDestroyView()
    }

    private fun setupBottomSheet() {
        binding.rvBottomSheetPlaylists.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBottomSheetPlaylists.adapter = playlistsAdapter

        bottomSheetBehavior = BottomSheetBehavior.from(binding.playlistsBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
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

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistsAdapter.submitList(playlists)
            binding.tvBottomSheetEmpty.visibility = if (playlists.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.addToPlaylistResult.observe(viewLifecycleOwner) { result ->
            val messageRes = when (result) {
                is AddToPlaylistResult.Added -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    R.string.added_to_playlist
                }
                is AddToPlaylistResult.AlreadyAdded -> R.string.already_added_to_playlist
            }
            val playlistName = when (result) {
                is AddToPlaylistResult.Added -> result.playlistName
                is AddToPlaylistResult.AlreadyAdded -> result.playlistName
            }
            Toast.makeText(
                requireContext(),
                getString(messageRes, playlistName),
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.openCreatePlaylistEvent.observe(viewLifecycleOwner) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(R.id.action_audioPlayerFragment_to_createPlaylistFragment)
        }
    }

    companion object {
        const val ARG_TRACK_ID = "trackId"
    }
}
