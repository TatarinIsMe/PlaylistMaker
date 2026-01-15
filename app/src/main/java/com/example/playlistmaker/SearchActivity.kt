package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity() {
    private lateinit var flContent: View

    private lateinit var etSearch: EditText
    private lateinit var btnClear: ImageButton

    private lateinit var ivBack: ImageView
    private var currentQuery: String = ""

    private lateinit var rvTracks: RecyclerView

    private lateinit var llEmptyPlaceholder: View
    private lateinit var llErrorPlaceholder: View
    private lateinit var btnRetry: Button
    private var lastFailedQuery: String = ""

    private lateinit var llHistory: View
    private lateinit var rvHistory: RecyclerView
    private lateinit var btnClearHistory: Button

    private lateinit var historyAdapter: TrackAdapter
    private lateinit var searchHistory: SearchHistory

    private val handler = Handler(Looper.getMainLooper())
    private val SEARCH_DEBOUNCE_DELAY = 2000L
    private var searchRunnable: Runnable? = null

    private lateinit var progressBar: ProgressBar

    private val CLICK_DEBOUNCE_DELAY = 1000L
    private var isClickAllowed = true


    private val adapter by lazy {
        TrackAdapter(onItemClick = { track -> onTrackClicked(track) })
    }






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPrefs = getSharedPreferences("playlist_maker_prefs", MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPrefs)

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
        llEmptyPlaceholder = findViewById<View>(R.id.llEmptyPlaceholder)
        llErrorPlaceholder = findViewById<View>(R.id.llErrorPlaceholder)
        btnRetry = findViewById<Button>(R.id.btnRetry)

        etSearch.setOnFocusChangeListener { _, _ ->
            updateHistoryVisibility()
        }
        btnClearHistory.setOnClickListener {
            searchHistory.clear()
            llHistory.visibility = View.GONE
            historyAdapter.submitList(emptyList())
        }


        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString().orEmpty()
                btnClear.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
                updateHistoryVisibility()

                searchRunnable?.let { handler.removeCallbacks(it) }
                if (currentQuery.isBlank()) return

                searchRunnable = Runnable { performSearch(currentQuery) }
                handler.postDelayed(searchRunnable!!, SEARCH_DEBOUNCE_DELAY)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (currentQuery.isNotBlank()) {
                    performSearch(currentQuery)
                }
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
            if (lastFailedQuery.isNotBlank()) {
                performSearch(lastFailedQuery)
            }
        }
    }
    private fun performSearch(query: String) {
        hideKeyboard()
        lastFailedQuery = query

        showSearchResults()

        rvTracks.visibility = View.GONE
        llEmptyPlaceholder.visibility = View.GONE
        llErrorPlaceholder.visibility = View.GONE

        showLoading()


        ItunesService.api.searchTracks(query)
            .enqueue(object : Callback<TracksSearchResponse> {
                override fun onResponse(
                    call: Call<TracksSearchResponse>,
                    response: Response<TracksSearchResponse>
                ) {
                    hideLoading()
                    if (!response.isSuccessful) {
                        showErrorPlaceholder()
                        return
                    }

                    val body = response.body()
                    val dtos = body?.results.orEmpty()
                    val tracks = dtos.map { it.toTrack() }

                    if (tracks.isEmpty()) {
                        showEmptyPlaceholder()
                    } else {
                        showTracks(tracks)
                    }
                }

                override fun onFailure(call: Call<TracksSearchResponse>, t: Throwable) {
                    hideLoading()
                    showErrorPlaceholder()
                }
            })
    }

    private fun showTracks(tracks: List<Track>) {
        showSearchResults()

        adapter.submitList(tracks)
        rvTracks.visibility = View.VISIBLE
        llEmptyPlaceholder.visibility = View.GONE
        llErrorPlaceholder.visibility = View.GONE
    }

    private fun showEmptyPlaceholder() {
        showSearchResults()

        adapter.submitList(emptyList())
        rvTracks.visibility = View.GONE
        llEmptyPlaceholder.visibility = View.VISIBLE
        llErrorPlaceholder.visibility = View.GONE
    }

    private fun showErrorPlaceholder() {
        showSearchResults()

        adapter.submitList(emptyList())
        rvTracks.visibility = View.GONE
        llEmptyPlaceholder.visibility = View.GONE
        llErrorPlaceholder.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY, currentQuery)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentQuery = savedInstanceState.getString(KEY).orEmpty()
        etSearch.setText(currentQuery)
        etSearch.setSelection(currentQuery.length)
        btnClear.visibility = if (currentQuery.isEmpty()) View.GONE else View.VISIBLE
    }
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        (currentFocus ?: window.decorView).windowToken?.let {
            imm.hideSoftInputFromWindow(it, 0)
        }
    }
    private fun TrackDto.toTrack(): Track {
        return Track(
            trackId = trackId ?: 0L,
            trackName = trackName.orEmpty(),
            artistName = artistName.orEmpty(),
            trackTimeMillis = trackTimeMillis ?: 0L,
            artworkUrl100 = artworkUrl100,

            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            previewUrl = previewUrl
        )
    }


    private fun updateHistoryVisibility() {
        val hasFocus = etSearch.hasFocus()
        val textEmpty = etSearch.text.isNullOrEmpty()
        val history = searchHistory.getHistory()

        if (hasFocus && textEmpty && history.isNotEmpty()) {
            historyAdapter.submitList(history)
            showHistory()
        } else {
            llHistory.visibility = View.GONE
        }
    }
    private fun onTrackClicked(track: Track) {
        if (!clickDebounce()) return

        searchHistory.addTrack(track)

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

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
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
        searchRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }
    companion object {
        private const val KEY = "key_search_query"
    }



}