package com.example.playlistmaker.presentation.media

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.playlistmaker.R

class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {
    private val viewModel: PlaylistsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(): PlaylistsFragment = PlaylistsFragment()
    }
}
