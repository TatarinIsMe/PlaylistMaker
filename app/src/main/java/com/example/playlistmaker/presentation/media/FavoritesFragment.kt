package com.example.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentFavoritesBinding
import com.example.playlistmaker.domain.model.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.player.AudioPlayerFragment
import com.example.playlistmaker.presentation.search.TrackAdapter

class FavoritesFragment : Fragment() {
    private val viewModel: FavoritesViewModel by viewModel()
    private var _binding: FragmentFavoritesBinding? = null
    private val binding: FragmentFavoritesBinding
        get() = _binding ?: error("Binding is only valid between onCreateView and onDestroyView")

    private val adapter by lazy {
        TrackAdapter(onItemClick = ::onTrackClicked)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = adapter

        viewModel.state.observe(viewLifecycleOwner, ::renderState)
        viewModel.navigationEvent.observe(viewLifecycleOwner, ::navigateToPlayer)
    }

    override fun onDestroyView() {
        binding.rvFavorites.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun onTrackClicked(track: Track) {
        viewModel.onTrackClicked(track)
    }

    private fun renderState(state: FavoritesState) {
        when (state) {
            FavoritesState.Empty -> {
                binding.llPlaceholder.visibility = View.VISIBLE
                binding.rvFavorites.visibility = View.GONE
                adapter.submitList(emptyList())
            }

            is FavoritesState.Content -> {
                binding.llPlaceholder.visibility = View.GONE
                binding.rvFavorites.visibility = View.VISIBLE
                adapter.submitList(state.tracks)
            }
        }
    }

    private fun navigateToPlayer(trackId: Long) {
        findNavController().navigate(
            R.id.action_mediaFragment_to_audioPlayerFragment,
            bundleOf(AudioPlayerFragment.ARG_TRACK_ID to trackId)
        )
    }

    companion object {
        fun newInstance(): FavoritesFragment = FavoritesFragment()
    }
}
