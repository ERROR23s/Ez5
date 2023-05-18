package com.example.musicalbum2

import android.content.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicalbum2.data.Music
import com.example.musicalbum2.`interface`.onSongComplete
import com.example.musicalbum2.`interface`.onSongSelect
import com.example.musicalbum2.list.ListAdapter
import com.example.musicalbum2.services.MusicService
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : AppCompatActivity(),onSongSelect,onSongComplete,View.OnClickListener {
    lateinit var list: ArrayList<Music>
    lateinit var adapter: ListAdapter
    lateinit var musicService: MusicService
    lateinit var seekBar: SeekBar
    lateinit var music_layout:ConstraintLayout
    var playintent: Intent?=null
    lateinit var Music:Music
    lateinit var playbtn:ImageButton
    lateinit var skipFbtn:ImageButton
    lateinit var skipBbtn:ImageButton
    lateinit var colapsebtn:ImageView
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Toast.makeText(this,"Сделал Павел Шоно 20-КИС-2",Toast.LENGTH_LONG).show()
        supportActionBar?.hide()
        list = ArrayList()

        adapter = ListAdapter(applicationContext,this)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter=adapter
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        music_layout = findViewById(R.id.music_layout)
        playbtn = findViewById(R.id.imageButton2)
        skipFbtn = findViewById(R.id.imageButton3)
        skipBbtn = findViewById(R.id.imageButton4)
        colapsebtn = findViewById(R.id.colapse_btn)
        bottomSheetBehavior = BottomSheetBehavior.from(music_layout)

        playbtn.setOnClickListener(this)
        skipFbtn.setOnClickListener(this)
        skipBbtn.setOnClickListener(this)
        colapsebtn.setOnClickListener(this)

        getSong()
        var search_text =  findViewById<EditText>(R.id.search_text)
        search_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchSong(p0.toString())
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

    }

    private fun getSong() {
         val songUri:Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
         val contentResolver: ContentResolver = this.contentResolver
         val cursor = contentResolver.query(songUri, null, null, null, null)

         if (cursor != null && cursor.moveToFirst()) {
             list.clear()
             val songId = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
             val songTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
             val songArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
             val songData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
             val date = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
             val albumColum = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
             while (cursor.moveToNext()) {
                 val currentId = cursor.getLong(songId)
                 var song_title = cursor.getString(songTitle)
                 var song_artist = cursor.getString(songArtist)
                 var song_data = cursor.getString(songData)
                 val song_date = cursor.getLong(date)
                 val albumId = cursor.getLong(albumColum)

                 val IMAGE_Uri = Uri.parse("content://media/external/audio/albumart")
                 val albumUri = ContentUris.withAppendedId(IMAGE_Uri, albumId)
                 list.add( Music(
                     currentId,
                     song_title,
                     song_artist,
                     song_data,
                     song_date,
                     albumUri,
                 ))

             }
             adapter.setData(list)
         }
    }
    private fun searchSong(value: String){
        var songList= ArrayList<Music>()
        for (song in list){
            var checker = true
            if(song.song_title.lowercase().contains(value.lowercase())){
                songList.add(song)
                checker = false
            }
            if(song.artist.lowercase().contains(value.lowercase())&&checker){
                songList.add(song)
            }
        }
        adapter.updateData(songList)
    }

    override fun onStart() {
        super.onStart()
        if(playintent==null){
            playintent = Intent(this, MusicService::class.java)
            bindService(playintent,musicConection, Context.BIND_AUTO_CREATE)
            startService(playintent)
        }
    }

    override fun onDestroy() {
        stopService(playintent)
        unbindService(musicConection)
        super.onDestroy()
    }
    private fun updateUI(){
        music_layout.findViewById<TextView>(R.id.songName).text = Music.song_title
        music_layout.findViewById<TextView>(R.id.authorName).text = Music.artist
        music_layout.findViewById<ImageView>(R.id.imageView).setImageURI(Music.image)

    }

    private var musicConection:ServiceConnection = object :ServiceConnection, onSongComplete {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder:MusicService.MusicBinder=p1 as MusicService.MusicBinder
            musicService = binder.service
            musicService.setUI(music_layout.findViewById(R.id.seekBar),
                music_layout.findViewById(R.id.StartTxt),
                music_layout.findViewById(R.id.EndTxt),
                )
            musicService.setListner(this)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
        }

        override fun onsongComplete() {
            if (currentSong != -1) {
                if (currentSong == 0) currentSong = list.size - 1 else currentSong--
                musicService.setMusic(list[currentSong])
                Music = list[currentSong]
                updateUI()
            }
        }

    }
    override fun onsongComplete() {
        if (currentSong != -1) {
            if (currentSong == 0) currentSong = list.size - 1 else currentSong--
            musicService.setMusic(list[currentSong])
            Music = list[currentSong]
            updateUI()
        }
    }

    override fun onSelect(music: Music) {
        musicService.setMusic(music)
        Music=music

        updateUI()
    }

    override fun onClick(p0: View?) {
        when(p0){
            playbtn->{
                if(musicService.playerState==2){ //resume
                    playbtn.setImageResource(R.drawable.ic_play)
                    musicService.pauseSong()
                }else if (musicService.playerState==1){ //pause
                    playbtn.setImageResource(R.drawable.ic_pause)
                    musicService.resumeSong()
                }
            }
            skipBbtn->{
                if(list.size>0){
                    if(currentSong!=-1){
                        if (currentSong!= 0)  currentSong = list.size-1 else --currentSong

                        musicService.setMusic(list[currentSong])
                        Music = list[currentSong]
                        updateUI()
                    }
                }
            }
            skipFbtn->{
                if(list.size>0){
                    if(currentSong!=-1) {
                        if (list.size - 1 > 0) currentSong = 0 else currentSong++

                        musicService.setMusic(list[currentSong])
                        Music = list[currentSong]
                        updateUI()
                    }
                }
            }
            colapsebtn ->{
                if(BottomSheetBehavior.STATE_EXPANDED==bottomSheetBehavior.state) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }else{
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }
}
