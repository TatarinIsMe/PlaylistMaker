package com.example.playlistmaker.presentation.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.player.AudioPlayerFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModel()
    private var _binding: ActivitySearchBinding? = null
    private val binding: ActivitySearchBinding
        get() = _binding ?: error("Binding is only valid between onCreateView and onDestroyView")

    private val adapter by lazy { TrackAdapter(onItemClick = ::onTrackClicked) }
    private lateinit var historyAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyAdapter = TrackAdapter(onItemClick = ::onTrackClicked)
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = historyAdapter

        binding.rvTracks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTracks.adapter = adapter

        binding.etSearch.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onSearchFieldFocusChanged(hasFocus)
        }

        binding.btnClearHistory.setOnClickListener {
            viewModel.onClearHistory()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString().orEmpty()
                binding.btnClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                viewModel.onSearchQueryChanged(query)
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.onSearchSubmitted()
                true
            } else {
                false
            }
        }

        binding.btnClear.setOnClickListener {
            binding.etSearch.text?.clear()
            hideKeyboard()
            binding.btnClear.visibility = View.GONE
            binding.etSearch.clearFocus()

            adapter.submitList(emptyList())
            binding.rvTracks.visibility = View.GONE
            binding.llEmptyPlaceholder.visibility = View.GONE
            binding.llErrorPlaceholder.visibility = View.GONE
        }

        binding.btnRetry.setOnClickListener {
            viewModel.onRetrySearch()
        }

        observeViewModel()
    }

    override fun onDestroyView() {
        binding.rvTracks.adapter = null
        binding.rvHistory.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner, ::renderState)
        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let(::navigateToPlayer)
        }
    }

    private fun renderState(state: SearchUiState) {
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        historyAdapter.submitList(state.history)
        if (state.isHistoryVisible) {
            showHistory()
        } else {
            showSearchResults()
        }

        adapter.submitList(state.tracks)
        binding.rvTracks.visibility = if (state.tracks.isNotEmpty()) View.VISIBLE else View.GONE

        binding.llEmptyPlaceholder.visibility = if (state.showEmptyPlaceholder) View.VISIBLE else View.GONE
        binding.llErrorPlaceholder.visibility = if (state.showErrorPlaceholder) View.VISIBLE else View.GONE
    }

    private fun onTrackClicked(track: Track) {
        viewModel.onTrackSelected(track)
    }

    private fun navigateToPlayer(trackId: Long) {
        findNavController().navigate(
            R.id.action_searchFragment_to_audioPlayerFragment,
            bundleOf(AudioPlayerFragment.ARG_TRACK_ID to trackId)
        )
    }

    private fun showHistory() {
        binding.llHistory.visibility = View.VISIBLE
        binding.flContent.visibility = View.GONE
    }

    private fun showSearchResults() {
        binding.llHistory.visibility = View.GONE
        binding.flContent.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val token = activity?.currentFocus?.windowToken ?: binding.root.windowToken ?: return
        imm.hideSoftInputFromWindow(token, 0)
    }
}
