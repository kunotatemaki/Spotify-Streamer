package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
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

    //media player
    private MediaPlayer player;
    //song list
    private List<ListItem> songs;
    //current position
    private int songPosn;

    List<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_PLAY = 3;
    static final int MSG_PAUSE = 4;
    static final int MSG_STOP = 5;
    static final int MSG_NEXT = 6;
    static final int MSG_PREV = 7;
    static final int MSG_SET_CURRENT_SONG = 8;
    static final int MSG_SET_SONG_LIST = 9;
    static final int MSG_SET_AS_FOREGROUND  = 10;
    static final int MSG_FINISHED_PLAYING_SONG = 11;
    static final int MSG_PLAYING_SONG = 12;
    static final int MSG_PAUSED_SONG = 13;
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.


    private final IBinder musicBind = new MusicBinder();


    @Override
    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        songPosn=0;
        //create player
        player = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        //set player properties
        //wakelock
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //wifi lock
        WifiManager.WifiLock wifiLock = ((WifiManager) getSystemService(getApplicationContext().WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();
        //TODO liberar el wifilock cuando proceda

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
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_SONG_LIST:
                    List<ListItem> songs = msg.getData().getParcelableArrayList("list_of_songs");
                    setList(songs);
                    break;
                case MSG_PLAY:
                    playSong();
                    break;
                case MSG_SET_CURRENT_SONG:
                    songPosn = msg.arg1;
                    break;
                case MSG_SET_AS_FOREGROUND:
                    setAsForeground();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        LogHelper.d(TAG, "onCompletion");
        sendFinisihedPlayingSong(songPosn);
        songPosn++;
        if(songPosn < songs.size()) playSong();
        else releaseMediaPlayer();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
        sendSongPlaying(songPosn);
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    private void playSong(){
        //play a song
        player.reset();
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
        player.prepareAsync();

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
            player.stop();
            player.release();
        }
    }

    private void sendFinisihedPlayingSong(int currentSong){
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_FINISHED_PLAYING_SONG, currentSong, 0));
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void sendSongPlaying(int currentSong){
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_PLAYING_SONG, currentSong, 0));
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void sendSongPaused(int currentSong){
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_PAUSED_SONG, currentSong, 0));
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }


}

