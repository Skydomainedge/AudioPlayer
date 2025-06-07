package org.wit.audioplayer.ui.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.wit.audioplayer.R
import org.wit.audioplayer.data.local.entity.AudioTrack

class AudioTrackAdapter : RecyclerView.Adapter<AudioTrackAdapter.AudioTrackViewHolder>() {

    private var tracks = listOf<AudioTrack>()

    fun submitList(newList: List<AudioTrack>) {
        tracks = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioTrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audio_track, parent, false)
        return AudioTrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioTrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size

    class AudioTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvArtist: TextView = itemView.findViewById(R.id.tvArtist)

        fun bind(track: AudioTrack) {
            tvTitle.text = track.title
            tvArtist.text = track.artist ?: "Unknown Artist"
        }
    }
}
