package com.example.playlistmaker.presentation.media

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemPlaylistBinding
import com.example.playlistmaker.domain.model.Playlist

class PlaylistsAdapter : ListAdapter<Playlist, PlaylistsAdapter.PlaylistViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPlaylistBinding.inflate(inflater, parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlaylistViewHolder(
        private val binding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.tvPlaylistName.text = playlist.name
            binding.tvPlaylistTracksCount.text = itemView.resources.getQuantityString(
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
