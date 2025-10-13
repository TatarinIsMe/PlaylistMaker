package com.example.playlistmaker

import android.content.res.ColorStateList
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
    private val items: MutableList<Track> = mutableListOf()
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    fun submitList(newItems: List<Track>) {
        items.clear()
        items.addAll(newItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCover: ShapeableImageView = itemView.findViewById(R.id.ivCover)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        private val ivChevron: ImageView = itemView.findViewById(R.id.ivChevron)

        fun bind(track: Track, position: Int) {
            tvTitle.text = track.trackName
            tvSubtitle.text = "${track.artistName} • ${track.trackTime}"

            val radius = itemView.resources.getDimensionPixelSize(R.dimen.cover_radius)
            Glide.with(itemView)
                .load(track.artworkUrl100)
                .transform(RoundedCorners(radius))
                .placeholder(R.drawable.ic_placeholder_45)
                .error(R.drawable.ic_placeholder_45)
                .into(ivCover)


            itemView.setOnClickListener {

            }
        }
    }
}
