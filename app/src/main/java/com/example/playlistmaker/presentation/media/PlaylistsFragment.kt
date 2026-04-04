package com.example.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {
    private val viewModel: PlaylistsViewModel by viewModel()
    private var _binding: FragmentPlaylistsBinding? = null
    private val binding: FragmentPlaylistsBinding
        get() = _binding ?: error("Binding is only valid between onCreateView and onDestroyView")

    private val adapter by lazy {
        PlaylistsAdapter { playlist ->
            findNavController().navigate(
                R.id.action_mediaFragment_to_playlistFragment,
                bundleOf(ARG_PLAYLIST_ID to playlist.playlistId)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvPlaylists.adapter = adapter

        binding.btnNewPlaylist.setOnClickListener {
            findNavController().navigate(R.id.action_mediaFragment_to_createPlaylistFragment)
        }

        viewModel.state.observe(viewLifecycleOwner, ::renderState)
    }

    override fun onDestroyView() {
        binding.rvPlaylists.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun renderState(state: PlaylistsState) {
        when (state) {
            PlaylistsState.Empty -> {
                binding.llPlaylistsPlaceholder.visibility = View.VISIBLE
                binding.rvPlaylists.visibility = View.GONE
                adapter.submitList(emptyList())
            }

            is PlaylistsState.Content -> {
                binding.llPlaylistsPlaceholder.visibility = View.GONE
                binding.rvPlaylists.visibility = View.VISIBLE
                adapter.submitList(state.playlists)
            }
        }
    }

    companion object {
        private const val ARG_PLAYLIST_ID = "playlistId"

        fun newInstance(): PlaylistsFragment = PlaylistsFragment()
    }
}
