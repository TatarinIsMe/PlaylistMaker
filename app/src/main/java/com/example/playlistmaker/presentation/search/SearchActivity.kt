package com.example.playlistmaker.presentation.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.App
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.player.AudioPlayerActivity

class SearchActivity : AppCompatActivity() {
    private val creator by lazy { (applicationContext as App).creator }
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(
            creator.searchInteractor,
            creator.searchHistoryInteractor
        )
    }

    private lateinit var flContent: View
    private lateinit var etSearch: EditText
    private lateinit var btnClear: ImageButton
    private lateinit var ivBack: ImageView
    private lateinit var rvTracks: RecyclerView
    private lateinit var llEmptyPlaceholder: View
    private lateinit var llErrorPlaceholder: View
    private lateinit var btnRetry: Button
    private lateinit var llHistory: View
    private lateinit var rvHistory: RecyclerView
    private lateinit var btnClearHistory: Button
    private lateinit var progressBar: ProgressBar

    private val handler = Handler(Looper.getMainLooper())
    private val CLICK_DEBOUNCE_DELAY = 1000L
    private var isClickAllowed = true
    private val adapter by lazy { TrackAdapter(onItemClick = { track -> onTrackClicked(track) }) }
    private lateinit var historyAdapter: TrackAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressBar = findViewById(R.id.progressBar)
        flContent = findViewById(R.id.flContent)

        llHistory = findViewById(R.id.llHistory)
        rvHistory = findViewById(R.id.rvHistory)
        btnClearHistory = findViewById(R.id.btnClearHistory)

        historyAdapter = TrackAdapter(onItemClick = { track -> onTrackClicked(track) })
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter

        etSearch = findViewById(R.id.etSearch)
        btnClear = findViewById(R.id.btnClear)
        ivBack = findViewById(R.id.ivBack)
        rvTracks = findViewById(R.id.rvTracks)
        rvTracks.layoutManager = LinearLayoutManager(this)
        rvTracks.adapter = adapter
        llEmptyPlaceholder = findViewById(R.id.llEmptyPlaceholder)
        llErrorPlaceholder = findViewById(R.id.llErrorPlaceholder)
        btnRetry = findViewById(R.id.btnRetry)

        etSearch.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onSearchFieldFocusChanged(hasFocus)
        }

        btnClearHistory.setOnClickListener {
            viewModel.onClearHistory()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString().orEmpty()
                btnClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                viewModel.onSearchQueryChanged(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_SEARCH
            ) {
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

        ivBack.setOnClickListener { finish() }

        btnRetry.setOnClickListener {
            viewModel.onRetrySearch()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            renderState(state)
        }
        viewModel.navigationEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { navigateToPlayer(it) }
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
        if (!clickDebounce()) return
        viewModel.onTrackSelected(track)
    }

    private fun navigateToPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra(AudioPlayerActivity.EXTRA_TRACK, track)
        startActivity(intent)
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
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        (currentFocus ?: window.decorView).windowToken?.let {
            imm.hideSoftInputFromWindow(it, 0)
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
