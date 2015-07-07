package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
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

import com.bumptech.glide.Glide;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
    private Bitmap previousBitmap = null;

    List<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_FINISHING_SERVICE = 3;
    static final int MSG_SEEKBAR_POSITION  = 4;
    static final int MSG_FINISHED_PLAYING_SONG = 5;
    static final int MSG_PLAYING_SONG = 6;
    static final int MSG_PAUSED_SONG = 7;
    static final int MSG_BUFFERING = 8;
    static final int MSG_SONG_LIST_SENT = 9;

    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

    public static final String ACTION_PLAY = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_play";
    public static final String ACTION_PAUSE = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_pause";
    public static final String ACTION_NEXT = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_next";
    public static final String ACTION_PREVIOUS = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_previous";
    public static final String ACTION_SET_CURRENT_SONG = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_set_current_song";
    public static final String ACTION_RESUME = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_resume";
    public static final String ACTION_SEEK_TO_EXACT_POSITION = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_seek_to_exact_position";
    public static final String ACTION_ASK_CURRENT_PLAYING_SONG = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_ask_current_playing_song";
    public static final String ACTION_ASK_CURRENT_LIST = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_ask_current_list";
    public static final String ACTION_FINISH_SERVICE = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_finish_service";
    public static final String ACTION_SET_SONG_LIST = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicservice.action_set_song_list";

    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;

    //private final IBinder musicBind = new MusicBinder();
    private String artistId = "";
    WifiManager.WifiLock wifiLock;
    private Bitmap defaultBitmap;


    public class SeekBarUpdateBackgroundProcess implements Runnable {

        Thread backgroundThread;

        public void start() {
            if (backgroundThread == null) {
                backgroundThread = new Thread(this);
                backgroundThread.start();
            }
        }

        public void stop() {
            if (backgroundThread != null) {
                backgroundThread.interrupt();
            }
        }

        public void run() {
            try {
                LogHelper.d("Thread starting.");
                while (!backgroundThread.interrupted()) {
                    sendSeekBarPosition();
                    Thread.sleep(1000);
                }
                LogHelper.d("Thread stopping.");
            } catch (InterruptedException ex) {
                // important you respond to the InterruptedException and stop processing
                // when its thrown!  Notice this is outside the while loop.
                LogHelper.d("Thread shutting down");
            } finally {
                backgroundThread = null;
            }
        }
    }

    SeekBarUpdateBackgroundProcess seekBarUpdateBackgroundProcess;

    private class DownloadBitmapTask extends AsyncTask<Void, Void, Bitmap> {
        protected Bitmap doInBackground(Void... params) {
            try {
                Bitmap bitmap = Glide.with(getApplicationContext())
                        .load(currentPlayingSong.getThumbnailSmall())
                        .asBitmap()
                        .into(-1, -1)
                        .get();
                if(isCancelled()) return null;
                return bitmap;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Bitmap result) {
            if(result != null){
                previousBitmap = result;
                showNotification(false, false, ACTION_PAUSE);
            }
        }
    }

    DownloadBitmapTask downloadBitmapTask;

    private void handleIntent( Intent intent ) {
        if( intent == null || intent.getAction() == null )
            return;

        String action = intent.getAction();

        if( action.equalsIgnoreCase( ACTION_PLAY ) ) {
            playSong();
        }else if( action.equalsIgnoreCase( ACTION_PAUSE ) ) {
            pauseSong();
        }else if( action.equalsIgnoreCase( ACTION_PREVIOUS ) ) {
            skipToPrev();
        }else if( action.equalsIgnoreCase( ACTION_NEXT ) ) {
            skipToNext();
        }else if( action.equalsIgnoreCase( ACTION_FINISH_SERVICE ) ) {
            finishService();
        }else if( action.equalsIgnoreCase( ACTION_SET_CURRENT_SONG ) ) {
            songPosn = intent.getIntExtra(SONG_POSITION, 0);
        }else if( action.equalsIgnoreCase( ACTION_SEEK_TO_EXACT_POSITION ) ) {
            seekToExactPosition(intent.getIntExtra(SONG_POSITION, 0));
        }else if( action.equalsIgnoreCase( ACTION_ASK_CURRENT_PLAYING_SONG ) ) {
            if(currentPlayingSong != null)
                sendSongPlaying(currentPlayingSong, songPosn, false);
        }else if( action.equalsIgnoreCase( ACTION_ASK_CURRENT_LIST ) ) {
            if(songs != null)
                sendSongList();
        }else if( action.equalsIgnoreCase( ACTION_RESUME) ) {
            resumeSong();
        }else if( action.equalsIgnoreCase( ACTION_SET_SONG_LIST) ) {
            List<ListItem> _songs = intent.getParcelableArrayListExtra(SONG_LIST);
            if(intent.getExtras().containsKey(ARTIST_ID)){
                if(!artistId.equals(intent.getStringExtra(ARTIST_ID))){
                    //new artist id => new list of tracks. don't update play/pause events on current playing song
                    artistId = intent.getStringExtra(ARTIST_ID);
                    songPosn = -1;
                }
            }
            setList(_songs);
        }

    }

    /*private void initMediaSessions() {
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
                                     playSong();
                                 }
                                 @Override
                                 public void onPause() {
                                     super.onPause();
                                     LogHelper.d("MediaPlayerService", "onPause");
                                     pauseSong();
                                 }

                                 @Override
                                 public void onSkipToNext() {
                                     super.onSkipToNext();
                                     LogHelper.e( "MediaPlayerService", "onSkipToNext");
                                     skipToNext();
                                 }
                                 @Override
                                 public void onSkipToPrevious() {
                                     super.onSkipToPrevious();
                                     LogHelper.d( "MediaPlayerService", "onSkipToPrevious");
                                     skipToPrev();
                                 }
                             }
        );
    }*/

    /**
     * finish service
     */
    private void finishService(){
        //send message indicating the song playing is finished
        sendFinisihedPlayingSong(songPosn);
        sendFinisihingService();
        //finish service
        this.stopSelf();
    }

    private Notification.Action generateAction( int icon, String title, String intentAction ) {
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( intentAction );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder( icon, title, pendingIntent ).build();
    }



    private void updateIconNotification( ) {
        if(downloadBitmapTask != null) downloadBitmapTask.cancel(true);
        downloadBitmapTask = new DownloadBitmapTask();
        downloadBitmapTask.execute();
    }

    private void showNotification(boolean isForeground, boolean typeBuffering, String mainAction){
        Notification.MediaStyle style = new Notification.MediaStyle();

        //action if notification is clicked
        Intent launchIntent = new Intent( getApplicationContext(), FullScreenPlayerActivity.class );
        launchIntent.putExtra(MusicServiceActivity.START_CONNECTION, true);
        PendingIntent clickIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //action if notification is descarted
        Intent deleteIntent = new Intent( getApplicationContext(), MusicService.class );
        deleteIntent.setAction(ACTION_FINISH_SERVICE);
        PendingIntent deletePendingIntent = PendingIntent.getService(getApplicationContext(), 1, deleteIntent, 0);

        //load the largeIcon and set default drawable if null
        Bitmap largeIcon;
        if(typeBuffering || previousBitmap == null) {
            largeIcon = defaultBitmap;
        }else{
            largeIcon = previousBitmap;
        }

        int lockScreenVisibility;
        if(Utilities.getBooleanFromPreferences(getApplicationContext(), "pref_optionScreenOn")){
            lockScreenVisibility = Notification.VISIBILITY_PUBLIC;
        }else{
            lockScreenVisibility = Notification.VISIBILITY_SECRET;
        }

        String title;
        String text  = "";
        if(typeBuffering){
            title = getResources().getString(R.string.buffering);
        }else{
            title = currentPlayingSong.getTrackName();
            text = currentPlayingSong.getAlbumName();
        }

        Notification.Builder builder = new Notification.Builder( this )
                .setVisibility(lockScreenVisibility)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(title)
                .setContentText(text)
                .setDeleteIntent(deletePendingIntent)
                .setLargeIcon(largeIcon)
                .setContentIntent(clickIntent)
                .setStyle(style);

        if(mainAction != null) {
            builder.addAction(generateAction(android.R.drawable.ic_media_previous,
                    getResources().getString(R.string.prev_notification_button), ACTION_PREVIOUS));
            if(mainAction.equals(ACTION_PAUSE)) {
                builder.addAction(generateAction(android.R.drawable.ic_media_pause,
                        getResources().getString(R.string.pause_notification_button), ACTION_PAUSE));
            }else{
                builder.addAction(generateAction(android.R.drawable.ic_media_play,
                        getResources().getString(R.string.play_notification_button), ACTION_RESUME));
            }
            builder.addAction(generateAction(android.R.drawable.ic_media_next,
                    getResources().getString(R.string.next_notification_button), ACTION_NEXT));
            style.setShowActionsInCompactView(1);
        }


        if(isForeground) {
            startForeground(NOTIFICATION_ID, builder.build());
        }else {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, builder.build());
        }
        if(previousBitmap == null && !typeBuffering)
            updateIconNotification();
    }

    /**
     * set the service as a foreground service
     */
    private void setAsForeground(){
        showNotification(true, false, ACTION_PAUSE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if( mManager == null ) {
            mManager = (MediaSessionManager) this.getSystemService(Context.MEDIA_SESSION_SERVICE);
            //initMediaSessions();
            songPosn = 0;
            previouSong = songPosn;
            //create mMediaPlayer

            initMusicPlayer();
        }

        handleIntent( intent );
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate(){
        //create the service
        super.onCreate();
        defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
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
        //mSession.release();
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
                    }else{
                        //send nothing played
                        sendFinisihedPlayingSong(0);
                    }
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
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
        currentPlayingSong = songs.get(songPosn);
        previousBitmap = null;
        setAsForeground();
        executeTask();
        mp.start();
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
        showNotification(true, true, null);
        sendBufferingSong();
        adquireWifiLock();
        mMediaPlayer.prepareAsync();

    }

    private void resumeSong(){
        try {
            executeTask();
            mMediaPlayer.start();
            setAsForeground();
            sendSongPlaying(currentPlayingSong, songPosn, false);
            adquireWifiLock();
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
            showNotification(false, false, ACTION_RESUME);
            sendSongPaused(songPosn);
            releaseWifiLock();
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
        releaseWifiLock();
        stopForeground(true);
    }

    /**
     * adquires wifilock and create it if necessary
     */
    private void adquireWifiLock(){
        if(wifiLock == null){
            //initialize wifi lock
            wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        }
        //adquire
        if(!wifiLock.isHeld())
            wifiLock.acquire();

    }

    /**
     * release wifilock
     */
    private void releaseWifiLock(){
        if(wifiLock.isHeld())
            wifiLock.release();
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

    private void sendFinisihingService(){
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                Message msg = Message.obtain(null, MusicService.MSG_FINISHING_SERVICE);
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
        if(seekBarUpdateBackgroundProcess != null) {
            seekBarUpdateBackgroundProcess.stop();
            //seekBarUpdateBackgroundProcess = null;
        }
        /*if(seekBarUpdateTask != null) {
            seekBarUpdateTask.cancel(true);
            seekBarUpdateTask = null;
        }*/
    }

    private void executeTask(){
        if(seekBarUpdateBackgroundProcess == null)
            seekBarUpdateBackgroundProcess = new SeekBarUpdateBackgroundProcess();
        seekBarUpdateBackgroundProcess.start();
        /*seekBarUpdateTask = new SeekBarUpdateTask();
        seekBarUpdateTask.execute();*/
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

