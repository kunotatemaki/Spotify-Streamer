package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raúl Feliz Alonso on 30/06/15.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private final static String TAG = LogHelper.makeLogTag(MusicService.class);
    private static final int NOTIFICATION_ID = 999;
    public final static String SONG_INFO = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.songinfo";
    public final static String SONG_POSITION = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.songposition";
    public final static String SONG_LIST = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.songlist";
    public final static String ARTIST_ID = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.artistid";
    public final static String SEEKBAR_POSITION = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.seekbarposition";
    public static final String FIRST_SONG = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.firstsong";
    public static final String LAST_SONG = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.lastsong";

    //media player
    private MediaPlayer player;
    //song list
    private List<ListItem> songs;
    private ListItem currentPlayingSong;
    //current position
    private int songPosn;
    //previous song played
    private int previouSong;

    List<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_PLAY = 3;
    static final int MSG_PAUSE = 4;
    static final int MSG_SEEK_TO_EXACT_POSITION = 5;
    static final int MSG_NEXT = 6;
    static final int MSG_PREV = 7;
    static final int MSG_SET_CURRENT_SONG = 8;
    static final int MSG_SET_SONG_LIST = 9;
    static final int MSG_SEEKBAR_POSITION  = 10;
    static final int MSG_FINISHED_PLAYING_SONG = 11;
    static final int MSG_PLAYING_SONG = 12;
    static final int MSG_PAUSED_SONG = 13;
    static final int MSG_RESUME = 14;
    static final int MSG_BUFFERING = 15;

    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.


    //private final IBinder musicBind = new MusicBinder();
    private String artistId = "";
    WifiManager.WifiLock wifiLock;

    private class SeekBarUpdateTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            while(!isCancelled()){
                try {
                    Thread.sleep(1000);
                    publishProgress(null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return null;
        }

        protected void onProgressUpdate(Void... params) {
            sendSeekBarPosition();
        }


    }

    SeekBarUpdateTask seekBarUpdateTask;

    @Override
    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        songPosn = 0;
        previouSong = songPosn;
        //create player

        initMusicPlayer();
    }

    public void initMusicPlayer(){
        //set player properties
        player = new MediaPlayer();
        //wakelock
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //wifi lock
        wifiLock = ((WifiManager) getSystemService(getApplicationContext().WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        //Listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

    }

    /**
     * pass the track list to the service
     * @param theSongs list of tracks
     */
    private void setList(List<ListItem> theSongs){
        songs = theSongs;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        //return musicBind;
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent){
        //player.stop();
        //player.release();
        return false;
    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    if(player != null && player.isPlaying()) {
                        sendSongPlaying(currentPlayingSong, songPosn, false);
                    }
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_SONG_LIST:
                    List<ListItem> songs = msg.getData().getParcelableArrayList(SONG_LIST);
                    if(msg.getData().containsKey(ARTIST_ID)){
                        if(!artistId.equals(msg.getData().getString(ARTIST_ID))){
                            //new artist id => new list of tracks. don't update play/pause events on current playing song
                            artistId = msg.getData().getString(ARTIST_ID);
                            songPosn = 0;
                        }
                    }
                    setList(songs);
                    break;
                case MSG_PLAY:
                    playSong();
                    break;
                case MSG_PAUSE:
                    pauseSong();
                    break;
                case MSG_RESUME:
                    resumeSong();
                    break;
                case MSG_SET_CURRENT_SONG:
                    songPosn = msg.arg1;
                    break;
                case MSG_PREV:
                    skipToPrev();
                    break;
                case MSG_NEXT:
                    skipToNext();
                    break;
                case MSG_SEEK_TO_EXACT_POSITION:
                    seekToExactPosition(msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        cancelTask();
        LogHelper.d(TAG, "onCompletion");
        sendFinisihedPlayingSong(songPosn);
        songPosn++;
        if(songPosn < songs.size()){
            playSong();
        }else {
            releaseMediaPlayer();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        setAsForeground();
        executeTask();
        mp.start();
        currentPlayingSong = songs.get(songPosn);
        sendSongPlaying(currentPlayingSong, songPosn, true);
        previouSong = songPosn;
    }

    /*public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }*/

    private void playSong(){
        //play a song
        if(player != null)  player.reset();
        else    initMusicPlayer();
        sendFinisihedPlayingSong(previouSong);
        //get song
        if(songPosn >= songs.size()){
            Utilities.showToast(this, getResources().getString(R.string.song_no_available));
            return;
        }
        ListItem playSong = songs.get(songPosn);

        try{
            player.setDataSource(getApplicationContext(), Uri.parse(playSong.getPreviewUrl()));
        }
        catch(Exception e){
            LogHelper.e(TAG, "Error setting data source", e);
        }
        sendBufferingSong();
        wifiLock.acquire();
        player.prepareAsync();

    }

    private void resumeSong(){
        try {
            executeTask();
            player.start();
            setAsForeground();
            sendSongPlaying(currentPlayingSong, songPosn, false);
            wifiLock.acquire();
        }catch(IllegalStateException e){
            e.printStackTrace();
            playSong();     //reset and play again
        }
    }

    private void pauseSong(){
        try {
            cancelTask();
            player.pause();
            stopForeground(false);
            sendSongPaused(songPosn);
            wifiLock.release();
        }catch(IllegalStateException e){
            e.printStackTrace();
            player.reset(); //reset player
        }
    }


    @Override
    public void onDestroy() {
        releaseMediaPlayer();
        super.onDestroy();
    }

    /**
     * set the service as a foreground service
     */
    private void setAsForeground(){
        String songName;
        // assign the song name to songName
        //TODO abrir la activity de pantalla completa
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), SearchActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(
                this);
        Notification notification = builder.setContentIntent(pi)
                .setSmallIcon(android.R.drawable.ic_media_play).setTicker("Reproductor spotify ticker")
                .setAutoCancel(true).setContentTitle("reproductor spotify title")
                .setContentText("reproductor spotify content text").build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void releaseMediaPlayer(){
        if(player != null){
            cancelTask();
            player.stop();
            player.release();
            player = null;
        }
        wifiLock.release();
        stopForeground(true);
    }

    private void sendFinisihedPlayingSong(int currentSong){
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data
                Bundle bundle = new Bundle();
                bundle.putInt(SONG_POSITION, currentSong);
                bundle.putString(ARTIST_ID, artistId);
                Message msg = Message.obtain(null, MusicService.MSG_FINISHED_PLAYING_SONG);
                msg.setData(bundle);
                mClients.get(i).send(msg);
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    /**
     * send info to activity to show artist playing
     * @param currentSong position of the song in the tracklist
     */
    private void sendSongPlaying(ListItem song, int currentSong, boolean start){
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data
                Bundle bundle = new Bundle();
                bundle.putParcelable(SONG_INFO, song);
                bundle.putInt(SONG_POSITION, currentSong);
                bundle.putString(ARTIST_ID, artistId);
                if(songPosn == 0)   //first song, no prev
                    bundle.putBoolean(FIRST_SONG, true);
                if(songPosn == songs.size()-1)   //last song, no next
                    bundle.putBoolean(LAST_SONG, true);
                if(!start)
                    bundle.putInt(SEEKBAR_POSITION, player.getCurrentPosition());
                else
                    bundle.putInt(SEEKBAR_POSITION, 0);
                Message msg = Message.obtain(null, MusicService.MSG_PLAYING_SONG);
                msg.setData(bundle);
                mClients.get(i).send(msg);
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void sendSongPaused(Integer currentSong){
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data
                Bundle bundle = new Bundle();
                bundle.putInt(SONG_POSITION, currentSong);
                bundle.putString(ARTIST_ID, artistId);
                Message msg = Message.obtain(null, MusicService.MSG_PAUSED_SONG);
                msg.setData(bundle);
                mClients.get(i).send(msg);
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void sendBufferingSong(){
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_BUFFERING));
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void sendSeekBarPosition(){
        if(player == null)  return;
        int timePlayed = player.getCurrentPosition();
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_SEEKBAR_POSITION, timePlayed, 0));
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void cancelTask(){
        if(seekBarUpdateTask != null) {
            seekBarUpdateTask.cancel(true);
            seekBarUpdateTask = null;
        }
    }

    private void executeTask(){
        seekBarUpdateTask = new SeekBarUpdateTask();
        seekBarUpdateTask.execute();
    }

    private void skipToPrev(){
        cancelTask();
        LogHelper.d(TAG, "skipToPrev");
        sendFinisihedPlayingSong(songPosn);
        if(songPosn > 0) {
            songPosn--;
            if (songPosn < songs.size()) {
                playSong();
            } else {
                releaseMediaPlayer();
            }
        }
    }

    private void skipToNext(){
        cancelTask();
        LogHelper.d(TAG, "skipToNext");
        sendFinisihedPlayingSong(songPosn);
        songPosn++;
        if(songPosn < songs.size()){
            playSong();
        }else {
            releaseMediaPlayer();
        }
    }

    private void seekToExactPosition(int mseconds){
        try {
            player.seekTo(mseconds);
        }catch (IllegalStateException e){
            e.printStackTrace();
            player.reset();
        }
    }
}

