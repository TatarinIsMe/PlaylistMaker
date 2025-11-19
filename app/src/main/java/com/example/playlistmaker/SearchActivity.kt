package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
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


const val  KEY = "key_search_query"
class SearchActivity : AppCompatActivity() {
    private lateinit var etSearch: EditText
    private lateinit var btnClear: ImageButton

    private lateinit var ivBack: ImageView
    private var currentQuery: String = ""

    private lateinit var rvTracks: RecyclerView
    private val adapter by lazy { TrackAdapter() }

    private lateinit var llEmptyPlaceholder: View
    private lateinit var llErrorPlaceholder: View
    private lateinit var btnRetry: Button
    private var lastFailedQuery: String = ""




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        etSearch = findViewById(R.id.etSearch)
        btnClear = findViewById(R.id.btnClear)
        ivBack = findViewById(R.id.ivBack)
        rvTracks = findViewById(R.id.rvTracks)
        rvTracks.layoutManager = LinearLayoutManager(this)
        rvTracks.adapter = adapter
        llEmptyPlaceholder = findViewById<View>(R.id.llEmptyPlaceholder)
        llErrorPlaceholder = findViewById<View>(R.id.llErrorPlaceholder)
        btnRetry = findViewById<Button>(R.id.btnRetry)


        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString().orEmpty()
                btnClear.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
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
        rvTracks.visibility = View.GONE
        llEmptyPlaceholder.visibility = View.GONE
        llErrorPlaceholder.visibility = View.GONE


        ItunesService.api.searchTracks(query)
            .enqueue(object : Callback<TracksSearchResponse> {
                override fun onResponse(
                    call: Call<TracksSearchResponse>,
                    response: Response<TracksSearchResponse>
                ) {
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
                        Log.d("musics", tracks[1].trackName)
                        showTracks(tracks)
                    }
                }

                override fun onFailure(call: Call<TracksSearchResponse>, t: Throwable) {
                    showErrorPlaceholder()
                }
            })
    }

    private fun showTracks(tracks: List<Track>) {
        adapter.submitList(tracks)
        rvTracks.visibility = View.VISIBLE
        llEmptyPlaceholder.visibility = View.GONE
        llErrorPlaceholder.visibility = View.GONE
    }

    private fun showEmptyPlaceholder() {
        adapter.submitList(emptyList())
        rvTracks.visibility = View.GONE
        llEmptyPlaceholder.visibility = View.VISIBLE
        llErrorPlaceholder.visibility = View.GONE
    }

    private fun showErrorPlaceholder() {
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
    companion object {
        private const val KEY_QUERY = "SEARCH_QUERY_KEY"
    }
    private fun TrackDto.toTrack(): Track {
        val formattedTime = SimpleDateFormat("mm:ss", Locale.getDefault())
            .format(trackTimeMillis ?: 0L)
        return Track(
            trackName = trackName.orEmpty(),
            artistName = artistName.orEmpty(),
            trackTime = formattedTime,
            artworkUrl100 = artworkUrl100
        )
    }

}