package com.example.musicalbum2.list

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Video.Media
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.musicalbum2.R
import com.example.musicalbum2.currentSong
import com.example.musicalbum2.data.Music
import com.example.musicalbum2.`interface`.onSongSelect

class ListAdapter(var context: Context, var onSongSelect: onSongSelect): RecyclerView.Adapter<ListAdapter.MyViewHolder>() {
    var musicList= ArrayList<Music>()

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.custom_row, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentMusic = musicList[position]
        val song_tittle = holder.itemView.findViewById<TextView>(R.id.MusicName)
        val song_artist = holder.itemView.findViewById<TextView>(R.id.AutorName)
        val song_image = holder.itemView.findViewById<ImageView>(R.id.MusicIcn)

        song_tittle.text = currentMusic.song_title
        song_artist.text = currentMusic.artist
        try{
            val bitmap:Bitmap = BitmapFactory.decodeFile(currentMusic.image.toString())
            song_image.setImageBitmap(bitmap)
        } catch (e : Exception){
            //Toast.makeText(context,"Image not found $e", Toast.LENGTH_SHORT).show()
        }

        holder.itemView.setOnClickListener {
            onSongSelect.onSelect(musicList[position])
            currentSong = position
        }

    }
    fun setData(music:ArrayList<Music>){
        this.musicList += music
        notifyDataSetChanged()
    }
    fun updateData(list: ArrayList<Music>){
        musicList = list
        notifyDataSetChanged()
    }
}