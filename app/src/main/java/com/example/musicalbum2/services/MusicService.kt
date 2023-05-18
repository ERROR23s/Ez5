package com.example.musicalbum2.services

import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.widget.SeekBar
import android.widget.TextView
import com.example.musicalbum2.data.Music
import com.example.musicalbum2.`interface`.onSongComplete
import java.util.concurrent.TimeUnit

class MusicService:Service(), MediaPlayer.OnPreparedListener,MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener {
    lateinit var player:MediaPlayer
    lateinit var songs: Music
    lateinit var seekBar: SeekBar
    lateinit var start_point:TextView
    lateinit var end_point:TextView
    lateinit var onSongComplete: onSongComplete
    var playerState = stopped


    private val musicBinder = MusicBinder()
    private val interval:Long = 1000

    override fun onBind(p0: Intent?): IBinder? {
        return musicBinder
    }

    override fun onCreate() {
        super.onCreate()

        player = MediaPlayer()
        initMusic()
    }
    fun setListner(onsongComplete: onSongComplete){
        this.onSongComplete=onsongComplete
    }

    fun initMusic(){
        player.setWakeMode(applicationContext,PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)

    }



    inner class MusicBinder:Binder(){
        val service:MusicService
        get() = this@MusicService
    }

    override fun onUnbind(intent: Intent?): Boolean {
        player.stop()
        player.reset()
        player.release()
        return false
    }

    companion object{
        const val stopped = 0
        const val paused = 1
        const val playing = 2
    }

    override fun onPrepared(p0: MediaPlayer?) {
        p0!!.start()
        val duration=p0.duration
        seekBar.max=duration
        seekBar.postDelayed(progressRunner,interval.toLong())

        end_point.text = String.format(
            "%d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(duration.toLong())-
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration.toLong())))
    }

    public fun pauseSong(){
        player.pause()
        playerState = paused
        seekBar.removeCallbacks(progressRunner)
    }
    public fun resumeSong(){
        player.start()
        playerState = playing
        progressRunner.run()
    }


    private fun playSong(){
        player.reset()

        val playSong=songs
        val currentSongId = playSong.song_id
        val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSongId)

        player.setDataSource(applicationContext,trackUri)

        player.prepareAsync()
        progressRunner.run()
    }

    fun setUI(seekBar: SeekBar, start_int: TextView, end_int:TextView){
        this.seekBar = seekBar

        start_point = start_int
        end_point = end_int
        seekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2){
                    player.seekTo(p1)
                }

                start_point.text = String.format(
                    "%d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(p1.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(p1.toLong())-
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(p1.toLong())))
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

    }

    private val progressRunner:Runnable = object: Runnable{
        override fun run() {
            if(seekBar!=null){
                seekBar.progress = player.currentPosition
                if (player.isPlaying){
                    seekBar.postDelayed(this,interval)
                }
            }
        }
    }

    override fun onCompletion(p0: MediaPlayer?) {
        onSongComplete.onsongComplete()
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        return false
    }

    fun setMusic(music:Music) {
        songs = music
        playerState = playing
        playSong()

    }
}