package com.example.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMediaBinding
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaFragment : Fragment() {
    private val viewModel: MediaViewModel by viewModel()
    private var _binding: ActivityMediaBinding? = null
    private val binding: ActivityMediaBinding
        get() = _binding ?: error("Binding is only valid between onCreateView and onDestroyView")
    private var tabLayoutMediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = MediaPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 1

        tabLayoutMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_favorites)
                else -> getString(R.string.tab_playlists)
            }
        }.also { it.attach() }
    }

    override fun onDestroyView() {
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        binding.viewPager.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
