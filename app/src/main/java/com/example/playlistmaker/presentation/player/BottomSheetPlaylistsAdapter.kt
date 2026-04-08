package com.example.playlistmaker.presentation.player

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemBottomSheetPlaylistBinding
import com.example.playlistmaker.domain.model.Playlist

class BottomSheetPlaylistsAdapter(
    private val onItemClick: (Playlist) -> Unit
) : ListAdapter<Playlist, BottomSheetPlaylistsAdapter.PlaylistViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBottomSheetPlaylistBinding.inflate(inflater, parent, false)
        return PlaylistViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlaylistViewHolder(
        private val binding: ItemBottomSheetPlaylistBinding,
        private val onItemClick: (Playlist) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentPlaylist: Playlist? = null

        init {
            binding.root.setOnClickListener {
                currentPlaylist?.let(onItemClick)
            }
        }

        fun bind(playlist: Playlist) {
            currentPlaylist = playlist
            binding.tvPlaylistName.text = playlist.name
            binding.tvTracksCount.text = itemView.resources.getQuantityString(
                R.plurals.playlist_tracks_count,
                playlist.tracksCount,
                playlist.tracksCount
            )

            val coverUri = playlist.coverUri
            if (coverUri.isNullOrBlank()) {
                Glide.with(binding.root).clear(binding.ivPlaylistCover)
                binding.ivPlaylistCover.setImageResource(R.drawable.ic_placeholder_45)
                return
            }

            Glide.with(binding.root)
                .load(Uri.parse(coverUri))
                .placeholder(R.drawable.ic_placeholder_45)
                .error(R.drawable.ic_placeholder_45)
                .centerCrop()
                .into(binding.ivPlaylistCover)
        }
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Playlist>() {
            override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
                return oldItem.playlistId == newItem.playlistId
            }

            override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
                return oldItem == newItem
            }
        }
    }
}
