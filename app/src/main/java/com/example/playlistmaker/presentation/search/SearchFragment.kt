package com.example.playlistmaker.presentation.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.player.AudioPlayerFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment(R.layout.activity_search) {

    private val viewModel: SearchViewModel by viewModel()

    private lateinit var flContent: View
    private lateinit var etSearch: EditText
    private lateinit var btnClear: ImageButton
    private lateinit var rvTracks: RecyclerView
    private lateinit var llEmptyPlaceholder: View
    private lateinit var llErrorPlaceholder: View
    private lateinit var btnRetry: Button
    private lateinit var llHistory: View
    private lateinit var rvHistory: RecyclerView
    private lateinit var btnClearHistory: Button
    private lateinit var progressBar: ProgressBar

    private val adapter by lazy { TrackAdapter(onItemClick = ::onTrackClicked) }
    private lateinit var historyAdapter: TrackAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        flContent = view.findViewById(R.id.flContent)

        llHistory = view.findViewById(R.id.llHistory)
        rvHistory = view.findViewById(R.id.rvHistory)
        btnClearHistory = view.findViewById(R.id.btnClearHistory)

        historyAdapter = TrackAdapter(onItemClick = ::onTrackClicked)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = historyAdapter

        etSearch = view.findViewById(R.id.etSearch)
        btnClear = view.findViewById(R.id.btnClear)
        rvTracks = view.findViewById(R.id.rvTracks)
        rvTracks.layoutManager = LinearLayoutManager(requireContext())
        rvTracks.adapter = adapter
        llEmptyPlaceholder = view.findViewById(R.id.llEmptyPlaceholder)
        llErrorPlaceholder = view.findViewById(R.id.llErrorPlaceholder)
        btnRetry = view.findViewById(R.id.btnRetry)

        etSearch.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onSearchFieldFocusChanged(hasFocus)
        }

        btnClearHistory.setOnClickListener {
            viewModel.onClearHistory()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString().orEmpty()
                btnClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                viewModel.onSearchQueryChanged(query)
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.onSearchSubmitted()
                true
            } else {
                false
            }
        }

        btnClear.setOnClickListener {
            etSearch.text?.clear()
            hideKeyboard()
            btnClear.visibility = View.GONE
            etSearch.clearFocus()

            adapter.submitList(emptyList())
            rvTracks.visibility = View.GONE
            llEmptyPlaceholder.visibility = View.GONE
            llErrorPlaceholder.visibility = View.GONE
        }

        btnRetry.setOnClickListener {
            viewModel.onRetrySearch()
        }

        observeViewModel()
    }

    override fun onDestroyView() {
        rvTracks.adapter = null
        rvHistory.adapter = null
        super.onDestroyView()
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner, ::renderState)
        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let(::navigateToPlayer)
        }
    }

    private fun renderState(state: SearchUiState) {
        progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        historyAdapter.submitList(state.history)
        if (state.isHistoryVisible) {
            showHistory()
        } else {
            showSearchResults()
        }

        adapter.submitList(state.tracks)
        rvTracks.visibility = if (state.tracks.isNotEmpty()) View.VISIBLE else View.GONE

        llEmptyPlaceholder.visibility = if (state.showEmptyPlaceholder) View.VISIBLE else View.GONE
        llErrorPlaceholder.visibility = if (state.showErrorPlaceholder) View.VISIBLE else View.GONE
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
        llHistory.visibility = View.VISIBLE
        flContent.visibility = View.GONE
    }

    private fun showSearchResults() {
        llHistory.visibility = View.GONE
        flContent.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val token = (activity?.currentFocus ?: view)?.windowToken ?: return
        imm.hideSoftInputFromWindow(token, 0)
    }
}
