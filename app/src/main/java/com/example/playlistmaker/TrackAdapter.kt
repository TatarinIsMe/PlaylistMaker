package com.example.playlistmaker

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.color.MaterialColors

class TrackAdapter(
    private val items: MutableList<Track> = mutableListOf(),
    private val onItemClick: ((Track) -> Unit)? = null
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    fun submitList(newItems: List<Track>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = items[position]
        holder.bind(track)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(track)
        }
    }

    override fun getItemCount(): Int = items.size

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivCover: ShapeableImageView = itemView.findViewById(R.id.ivCover)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvArtist: TextView = itemView.findViewById(R.id.tvArtist)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(track: Track) {
            tvTitle.text = track.trackName
            tvArtist.text = track.artistName
            tvTime.text = track.getFormattedTime()

            val radius = itemView.resources.getDimensionPixelSize(R.dimen.cover_radius)

            Glide.with(itemView)
                .load(track.artworkUrl100)
                .transform(RoundedCorners(radius))
                .placeholder(R.drawable.ic_placeholder_45)
                .error(R.drawable.ic_placeholder_45)
                .into(ivCover)
        }
    }
}

