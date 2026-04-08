package com.example.playlistmaker.presentation.media.playlist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.media.create.CreatePlaylistFragment
import com.example.playlistmaker.presentation.player.AudioPlayerFragment
import com.example.playlistmaker.presentation.search.TrackAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlaylistFragment : Fragment() {

    private val playlistId: Long by lazy {
        val args = requireArguments()
        if (!args.containsKey(ARG_PLAYLIST_ID)) {
            error("Playlist ID argument is required")
        }
        args.getLong(ARG_PLAYLIST_ID).takeIf { it >= 0 } ?: error("Playlist ID argument is invalid")
    }

    private val viewModel: PlaylistViewModel by viewModel { parametersOf(playlistId) }
    private var _binding: FragmentPlaylistBinding? = null
    private val binding: FragmentPlaylistBinding
        get() = _binding ?: error("Binding is only valid between onCreateView and onDestroyView")

    private val tracksAdapter by lazy {
        TrackAdapter(
            onItemClick = viewModel::onTrackClicked,
            onItemLongClick = ::onTrackLongClicked
        )
    }
    private lateinit var tracksBottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private val menuBottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                binding.menuOverlay.visibility = View.GONE
                binding.menuOverlay.alpha = 0f
            } else {
                binding.menuOverlay.visibility = View.VISIBLE
                if (newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.menuOverlay.alpha = 1f
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val alpha = ((slideOffset + 1f) / 2f).coerceIn(0f, 1f)
            binding.menuOverlay.alpha = alpha
        }
    }

    private var renderedCoverUri: String? = null
    private var renderedMenuCoverUri: String? = null
    private var currentState = PlaylistUiState()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTracksBottomSheet()
        setupMenuBottomSheet()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnShare.setOnClickListener { sharePlaylist() }
        binding.btnMenu.setOnClickListener { showMenuBottomSheet() }
        binding.itemShareInMenu.setOnClickListener {
            hideMenuBottomSheet()
            sharePlaylist()
        }
        binding.itemEditInMenu.setOnClickListener {
            hideMenuBottomSheet()
            findNavController().navigate(
                R.id.action_playlistFragment_to_createPlaylistFragment,
                bundleOf(CreatePlaylistFragment.ARG_EDIT_PLAYLIST_ID to playlistId)
            )
        }
        binding.itemDeleteInMenu.setOnClickListener {
            hideMenuBottomSheet()
            showDeletePlaylistDialog()
        }
        binding.menuOverlay.setOnClickListener { hideMenuBottomSheet() }

        viewModel.uiState.observe(viewLifecycleOwner, ::renderState)
        viewModel.navigationEvent.observe(viewLifecycleOwner, ::navigateToPlayer)
        viewModel.playlistDeletedEvent.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        binding.rvPlaylistTracks.adapter = null
        Glide.with(this).clear(binding.ivPlaylistCover)
        Glide.with(this).clear(binding.ivMenuPlaylistCover)
        menuBottomSheetBehavior.removeBottomSheetCallback(menuBottomSheetCallback)
        _binding = null
        super.onDestroyView()
    }

    private fun renderState(state: PlaylistUiState) {
        currentState = state
        binding.tvPlaylistName.text = state.name
        binding.tvPlaylistDescription.text = state.description.orEmpty()
        binding.tvPlaylistDescription.isVisible = state.isDescriptionVisible

        binding.tvPlaylistDuration.text = resources.getQuantityString(
            R.plurals.playlist_minutes_count,
            state.totalDurationMinutes,
            state.totalDurationMinutes
        )
        binding.tvPlaylistTracksCount.text = resources.getQuantityString(
            R.plurals.playlist_tracks_count,
            state.tracksCount,
            state.tracksCount
        )
        binding.tvMenuPlaylistName.text = state.name
        binding.tvMenuPlaylistTracksCount.text = resources.getQuantityString(
            R.plurals.playlist_tracks_count,
            state.tracksCount,
            state.tracksCount
        )
        tracksAdapter.submitList(state.tracks)
        val hasTracks = state.tracks.isNotEmpty()
        binding.tracksBottomSheet.isVisible = hasTracks
        binding.tvTracksEmptyMessage.isVisible = !hasTracks
        updateTracksBottomSheetPeekHeight()
        if (hasTracks && tracksBottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        if (renderedCoverUri != state.coverUri) {
            renderedCoverUri = state.coverUri
            renderCover(state.coverUri)
        }
        if (renderedMenuCoverUri != state.coverUri) {
            renderedMenuCoverUri = state.coverUri
            renderMenuCover(state.coverUri)
        }
    }

    private fun renderCover(coverUri: String?) {
        if (coverUri.isNullOrBlank()) {
            Glide.with(this).clear(binding.ivPlaylistCover)
            binding.ivPlaylistCover.scaleType = ImageView.ScaleType.FIT_XY
            binding.ivPlaylistCover.setImageResource(R.drawable.ic_placeholder_45)
            return
        }

        binding.ivPlaylistCover.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(this)
            .load(Uri.parse(coverUri))
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .centerCrop()
            .into(binding.ivPlaylistCover)
    }

    private fun renderMenuCover(coverUri: String?) {
        if (coverUri.isNullOrBlank()) {
            Glide.with(this).clear(binding.ivMenuPlaylistCover)
            binding.ivMenuPlaylistCover.scaleType = ImageView.ScaleType.CENTER
            binding.ivMenuPlaylistCover.setImageResource(R.drawable.ic_placeholder_45)
            return
        }

        binding.ivMenuPlaylistCover.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(this)
            .load(Uri.parse(coverUri))
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .centerCrop()
            .into(binding.ivMenuPlaylistCover)
    }

    private fun setupTracksBottomSheet() {
        binding.rvPlaylistTracks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlaylistTracks.adapter = tracksAdapter

        tracksBottomSheetBehavior = BottomSheetBehavior.from(binding.tracksBottomSheet).apply {
            isHideable = false
            skipCollapsed = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
        updateTracksBottomSheetPeekHeight()
    }

    private fun updateTracksBottomSheetPeekHeight() {
        binding.root.post {
            if (!::tracksBottomSheetBehavior.isInitialized) return@post

            val rootHeight = binding.root.height
            if (rootHeight <= 0) return@post

            val topGapPx = (TOP_GAP_DP * resources.displayMetrics.density).toInt()
            val minPeekPx = (MIN_TRACKS_PEEK_DP * resources.displayMetrics.density).toInt()
            val preferredPeekHeight = rootHeight - (binding.btnShare.bottom + topGapPx)
            val resolvedPeekHeight = preferredPeekHeight
                .coerceAtLeast(minPeekPx)
                .coerceAtMost(rootHeight)

            if (tracksBottomSheetBehavior.peekHeight != resolvedPeekHeight) {
                tracksBottomSheetBehavior.peekHeight = resolvedPeekHeight
            }
        }
    }

    private fun setupMenuBottomSheet() {
        menuBottomSheetBehavior = BottomSheetBehavior.from(binding.menuBottomSheet).apply {
            isHideable = true
            skipCollapsed = false
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        menuBottomSheetBehavior.addBottomSheetCallback(menuBottomSheetCallback)
    }

    private fun onTrackLongClicked(track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_track_dialog_message)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.onDeleteTrackConfirmed(track)
            }
            .show()
    }

    private fun showDeletePlaylistDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_playlist_dialog_title)
            .setMessage(R.string.delete_playlist_dialog_message)
            .setNegativeButton(R.string.cancel_delete_playlist, null)
            .setPositiveButton(R.string.delete_playlist) { _, _ ->
                viewModel.onDeletePlaylistConfirmed()
            }
            .show()
    }

    private fun navigateToPlayer(trackId: Long) {
        findNavController().navigate(
            R.id.action_playlistFragment_to_audioPlayerFragment,
            bundleOf(AudioPlayerFragment.ARG_TRACK_ID to trackId)
        )
    }

    private fun sharePlaylist() {
        val tracks = currentState.tracks
        if (tracks.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.empty_playlist_share_message),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val text = buildString {
            appendLine(currentState.name)
            appendLine(currentState.description.orEmpty())
            appendLine(getString(R.string.share_playlist_tracks_count, currentState.tracksCount))
            tracks.forEachIndexed { index, track ->
                append(index + 1)
                append(". ")
                append(track.artistName)
                append(" - ")
                append(track.trackName)
                append(" (")
                append(track.getFormattedTime())
                appendLine(")")
            }
        }.trimEnd()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_playlist_chooser_title)))
    }

    private fun showMenuBottomSheet() {
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideMenuBottomSheet() {
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    companion object {
        private const val ARG_PLAYLIST_ID = "playlistId"
        private const val TOP_GAP_DP = 16
        private const val MIN_TRACKS_PEEK_DP = 120
    }
}
