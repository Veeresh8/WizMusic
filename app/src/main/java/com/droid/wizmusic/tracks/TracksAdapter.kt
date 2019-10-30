package com.droid.wizmusic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import kotlinx.android.synthetic.main.item_track.view.*

class TracksAdapter(private val clickInterface: ClickInterface) :
    androidx.recyclerview.widget.ListAdapter<Track, TracksAdapter.ViewHolder>(TracksDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_track, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTrack(getItem(position), clickInterface)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindTrack(track: Track, clickInterface: ClickInterface) {
            itemView.tvArtistName.text = track.artists
            itemView.tvTrackName.text = track.song
            itemView.ivCoverArt.apply {
                load(track.cover_image) {
                    crossfade(true)
                }
            }

            if (track.isPurchased) {
                itemView.tvBuy.gone()
            } else {
                itemView.tvBuy.visible()
            }

            itemView.setOnClickListener {
                clickInterface.onClick(track)
            }

            itemView.tvBuy.setOnClickListener {
                clickInterface.onBuy(track)
            }
        }
    }

    class TracksDiff : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem == newItem
        }
    }

    interface ClickInterface {
        fun onClick(track: Track)
        fun onBuy(track: Track)
    }
}
