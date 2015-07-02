package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
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

    //song list
    private List<ListItem> songs = new ArrayList<>();
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
    static final int MSG_ASK_CURRENT_PLAYING_SONG = 16;
    static final int MSG_ASK_CURRENT_LIST = 17;
    static final int MSG_SONG_LIST_SENT = 18;

    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

    public static final String ACTION_PLAY = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_play";
    public static final String ACTION_PAUSE = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_pause";
    public static final String ACTION_NEXT = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_next";
    public static final String ACTION_PREVIOUS = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_previous";

    //media mMediaPlayer
    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;
    private MediaSession mSession;
    private MediaController mController;

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



    private void handleIntent( Intent intent ) {
        if( intent == null || intent.getAction() == null )
            return;

        String action = intent.getAction();

        if( action.equalsIgnoreCase( ACTION_PLAY ) ) {
            mController.getTransportControls().play();
        } else if( action.equalsIgnoreCase( ACTION_PAUSE ) ) {
            mController.getTransportControls().pause();
        } else if( action.equalsIgnoreCase( ACTION_PREVIOUS ) ) {
            mController.getTransportControls().skipToPrevious();
        } else if( action.equalsIgnoreCase( ACTION_NEXT ) ) {
            mController.getTransportControls().skipToNext();
        }
    }

    private Notification.Action generateAction( int icon, String title, String intentAction ) {
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( intentAction );
        //TODO probar lo mismo para los otros botones, a ver si elimino mensajes
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder( icon, title, pendingIntent ).build();
    }

    private Notification buildNotification( Notification.Action action ) {
        Notification.MediaStyle style = new Notification.MediaStyle();

        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), FullScreenPlayerActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        //TODO load icon
        Bitmap theBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);

        Notification.Builder builder = new Notification.Builder( this )
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(currentPlayingSong.getTrackName())
                .setContentText(currentPlayingSong.getAlbumName())
                .setLargeIcon(theBitmap)
                .setContentIntent(pi)
                .setStyle(style);


        builder.addAction( generateAction( android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS ) );
        builder.addAction( action );
        builder.addAction( generateAction( android.R.drawable.ic_media_next, "Next", ACTION_NEXT ) );
        style.setShowActionsInCompactView(1);

       // NotificationManager notificationManager = (NotificationManager) getSystemService( this.NOTIFICATION_SERVICE );
       // notificationManager.notify( 1, builder.build() );
        return builder.build();
    }




    /**
     * set the service as a foreground service
     */
    private void setAsForeground(){
        String songName;
        // assign the song name to songName
        /*PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), FullScreenPlayerActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(
                this);
        Notification notification = builder.setContentIntent(pi)
                .setSmallIcon(android.R.drawable.ic_media_play).setTicker("Reproductor spotify ticker")
                .setAutoCancel(true).setContentTitle("reproductor spotify title")
                .setContentText("reproductor spotify content text").build();
        startForeground(NOTIFICATION_ID, notification);*/
        Notification notification = buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
        startForeground(NOTIFICATION_ID, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if( mManager == null ) {
            mManager = (MediaSessionManager) this.getSystemService(Context.MEDIA_SESSION_SERVICE);
            initMediaSessions();
        }

        handleIntent( intent );
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaSessions() {
        songPosn = 0;
        previouSong = songPosn;
        //create mMediaPlayer

        initMusicPlayer();

        mSession = new MediaSession(getApplicationContext(), "simple player session");
        mController =new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback(){
                 @Override
                 public void onPlay() {
                     super.onPlay();
                     LogHelper.d("MediaPlayerService", "onPlay");
                     //buildNotification( generateAction( android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE ) );
                 }

                 @Override
                 public void onPause() {
                     //super.onPause();
                     LogHelper.d("MediaPlayerService", "onPause");
                     //buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
                 }

                 @Override
                 public void onSkipToNext() {
                     //super.onSkipToNext();
                     LogHelper.e( "MediaPlayerService", "onSkipToNext");
                     skipToNext();
                     //Change media here
                     //buildNotification( generateAction( android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE ) );
                 }

                 @Override
                 public void onSkipToPrevious() {
                     //super.onSkipToPrevious();
                     LogHelper.d( "MediaPlayerService", "onSkipToPrevious");
                     skipToPrev();
                     //Change media here
                     //buildNotification( generateAction( android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE ) );
                 }

             }
        );
    }

    @Override
    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        songPosn = 0;
        previouSong = songPosn;
        //create mMediaPlayer

        initMusicPlayer();
    }

    public void initMusicPlayer(){
        //set mMediaPlayer properties
        mMediaPlayer = new MediaPlayer();
        //wakelock
        mMediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //wifi lock
        wifiLock = ((WifiManager) getSystemService(getApplicationContext().WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        //Listeners
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);

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
        //mMediaPlayer.stop();
        //mMediaPlayer.release();
        mSession.release();
        return false;
    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        sendSongPlaying(currentPlayingSong, songPosn, false);
                    }
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_SONG_LIST:
                    List<ListItem> _songs = msg.getData().getParcelableArrayList(SONG_LIST);
                    if(msg.getData().containsKey(ARTIST_ID)){
                        if(!artistId.equals(msg.getData().getString(ARTIST_ID))){
                            //new artist id => new list of tracks. don't update play/pause events on current playing song
                            artistId = msg.getData().getString(ARTIST_ID);
                            songPosn = -1;
                        }
                    }
                    setList(_songs);
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
                case MSG_ASK_CURRENT_PLAYING_SONG:
                    if(currentPlayingSong != null)
                        sendSongPlaying(currentPlayingSong, songPosn, false);
                    break;
                case MSG_ASK_CURRENT_LIST:
                    if(songs != null)
                        sendSongList();
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
        executeTask();
        mp.start();
        currentPlayingSong = songs.get(songPosn);
        setAsForeground();
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
        if(mMediaPlayer != null)  mMediaPlayer.reset();
        else    initMusicPlayer();
        sendFinisihedPlayingSong(previouSong);
        //get song
        if(songPosn >= songs.size()){
            Utilities.showToast(this, getResources().getString(R.string.song_no_available));
            return;
        }
        ListItem playSong = songs.get(songPosn);

        try{
            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(playSong.getPreviewUrl()));
        }
        catch(Exception e){
            LogHelper.e(TAG, "Error setting data source", e);
        }
        sendBufferingSong();
        wifiLock.acquire();
        mMediaPlayer.prepareAsync();

    }

    private void resumeSong(){
        try {
            executeTask();
            mMediaPlayer.start();
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
            mMediaPlayer.pause();
            stopForeground(false);
            sendSongPaused(songPosn);
            wifiLock.release();
        }catch(IllegalStateException e){
            e.printStackTrace();
            mMediaPlayer.reset(); //reset mMediaPlayer
        }
    }


    @Override
    public void onDestroy() {
        releaseMediaPlayer();
        super.onDestroy();
    }



    private void releaseMediaPlayer(){
        if(mMediaPlayer != null){
            cancelTask();
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
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
                if(currentPlayingSong != null)
                    bundle.putString(ARTIST_ID, currentPlayingSong.getArtistId());
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
     * @param song song info
     * @param currentSong position of the song in the tracklist
     * @param start send mMediaPlayer position if false, 0 if true;
     */
    private void sendSongPlaying(ListItem song, int currentSong, boolean start){
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data
                Bundle bundle = new Bundle();
                song.setDuration(mMediaPlayer.getDuration());
                bundle.putParcelable(SONG_INFO, song);
                bundle.putInt(SONG_POSITION, currentSong);
                if(songPosn == 0)   //first song, no prev
                    bundle.putBoolean(FIRST_SONG, true);
                if(songPosn == songs.size()-1)   //last song, no next
                    bundle.putBoolean(LAST_SONG, true);
                if(!start)
                    bundle.putInt(SEEKBAR_POSITION, mMediaPlayer.getCurrentPosition());
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

    /**
     * send list of tracks
     */
    private void sendSongList(){
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(SONG_LIST, (ArrayList<ListItem>)songs);
                Message msg = Message.obtain(null, MusicService.MSG_SONG_LIST_SENT);
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
                bundle.putString(ARTIST_ID, currentPlayingSong.getArtistId());
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
        if(mMediaPlayer == null)  return;
        int timePlayed = mMediaPlayer.getCurrentPosition();
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
            mMediaPlayer.seekTo(mseconds);
        }catch (IllegalStateException e){
            e.printStackTrace();
            mMediaPlayer.reset();
        }
    }
}

