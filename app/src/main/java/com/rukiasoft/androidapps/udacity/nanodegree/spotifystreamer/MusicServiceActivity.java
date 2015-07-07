package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.session.MediaController;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Base activity for activities that need to bind and communicate with the MusicService
 */
public class MusicServiceActivity extends ToolbarAndRefreshActivity {

    static final int STATE_PLAYING = 1;
    static final int STATE_PAUSED = 2;
    static final int STATE_STOPPED = 3;
    static final int STATE_BUFFERING = 4;
    private static final String STATE_ACTIVITY = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicserviceactivity.stateactivity";
    public static final String START_CONNECTION = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicserviceactivity.startconnection";
    private static final String TAG = LogHelper.makeLogTag(MusicServiceActivity.class);
    protected Boolean MusicServiceActivityVisible;

    //protected MusicService musicSrv;
    private Intent playIntent;
    protected boolean musicBound = false;
    Messenger mService = null;
    protected int currentSongState;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            switch (msg.what) {
                case MusicService.MSG_PAUSED_SONG:
                    bundle = msg.getData();
                    pausedSong(bundle);
                    break;
                case MusicService.MSG_PLAYING_SONG:
                    bundle = msg.getData();
                    playingSong(bundle);
                    break;
                case MusicService.MSG_FINISHED_PLAYING_SONG:
                    bundle = msg.getData();
                    finishedPlayingSong(bundle);
                    break;
                case MusicService.MSG_FINISHING_SERVICE:
                    finishingService();
                    break;
                case MusicService.MSG_BUFFERING:
                    bufferingSong();
                    break;
                case MusicService.MSG_SEEKBAR_POSITION:
                    seekBarPositionReceived(msg.arg1);
                    break;
                case MusicService.MSG_SONG_LIST_SENT:
                    bundle = msg.getData();
                    songListReceived(bundle);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            musicBound = true;
            try {
                Message msg = Message.obtain(null, MusicService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
                // In this case the service has crashed before we could even do anything with it
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            musicBound = false;
            try {
                Message msg = Message.obtain(null, MusicService.MSG_UNREGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
                // In this case the service has crashed before we could even do anything with it
            }
            playIntent = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVITY)){
            currentSongState = savedInstanceState.getInt(STATE_ACTIVITY);
        }
        if(getIntent().hasExtra(START_CONNECTION)) {
            //start connected. Show refresh and wait for service message to hide it
            currentSongState = STATE_BUFFERING;
            connectToService();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Save activity recreated
        savedInstanceState.putInt(STATE_ACTIVITY, currentSongState);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void connectToService(){
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, 0);
            startService(playIntent);
        }
    }

    @Override
    protected void onDestroy() {
        if(musicBound)
            unbindService(musicConnection);
        musicBound = false;
        if(mService != null) {
            try {
                Message msg = Message.obtain(null, MusicService.MSG_UNREGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                // In this case the service has crashed before we could even do anything with it
            }
        }
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();
        MusicServiceActivityVisible = true;
    }

    @Override
    public void onPostResume(){
        super.onPostResume();
        if(currentSongState == STATE_BUFFERING)
            showRefreshLayoutSwipeProgress();
    }

    @Override
    public void onPause(){
        super.onPause();
        MusicServiceActivityVisible = false;
    }

    protected void sendPlayMessageToService() {
        connectToService();
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( MusicService.ACTION_PLAY );
        startService(intent);
    }

    protected void sendPauseMessageToService() {
        connectToService();
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( MusicService.ACTION_PAUSE );
        startService(intent);
    }

    protected void sendResumeMessageToService() {
        connectToService();
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( MusicService.ACTION_RESUME );
        startService(intent);
    }

    protected void sendSkipToNextMessageToService() {
        connectToService();
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( MusicService.ACTION_NEXT );
        startService(intent);
    }

    protected void sendSkipToPrevMessageToService() {
        connectToService();
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( MusicService.ACTION_PREVIOUS );
        startService(intent);
    }

    protected void sendSetCurrentSongMessageToService(int position) {
        connectToService();
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( MusicService.ACTION_SET_CURRENT_SONG );
        intent.putExtra(MusicService.SONG_POSITION, position);
        startService(intent);
    }

    protected void sendSeekToPosition(int mseconds) {
        connectToService();
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( MusicService.ACTION_SEEK_TO_EXACT_POSITION );
        intent.putExtra(MusicService.SONG_POSITION, mseconds);
        startService(intent);
    }

    protected void sendSetSongListMessageToService(List<ListItem> songs, String id) {
        connectToService();
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( MusicService.ACTION_SET_SONG_LIST );
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MusicService.SONG_LIST, (ArrayList<ListItem>)songs);
        bundle.putString(MusicService.ARTIST_ID, id);
        intent.putExtras(bundle);
        startService(intent);
    }

    protected void sendAskForCurrentPlayingSongService(){
        connectToService();
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( MusicService.ACTION_ASK_CURRENT_PLAYING_SONG );
        startService(intent);
    }

    protected void sendAskForCurrentListService(){
        connectToService();
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( MusicService.ACTION_ASK_CURRENT_LIST );
        startService(intent);
    }

    /**
     * called when service send a message indicating that the song has been paused.
     * @param bundle info with the song paused
     */
    protected void pausedSong(Bundle bundle){
        currentSongState = STATE_PAUSED;
        hideRefreshLayoutSwipeProgress();
    }

    /**
     * called when service send a message indicating it starts playing the song.
     * @param bundle song's info
     */
    protected void playingSong(Bundle bundle){
        currentSongState = STATE_PLAYING;
        hideRefreshLayoutSwipeProgress();
    }

    /**
     * called when service send a message indicating the playing song ends
     * @param bundle song's info
     */
    protected void finishedPlayingSong(Bundle bundle){
        currentSongState = STATE_STOPPED;
        hideRefreshLayoutSwipeProgress();
    }

    /**
     * called when service send a message indicating it is buffering the song
     */
    protected void bufferingSong(){
        currentSongState = STATE_BUFFERING;
        showRefreshLayoutSwipeProgress();
    }

    /**
     * called when service send a message with the seekbar position
     * @param position of the seekbar
     */
    protected void seekBarPositionReceived(int position){

    }

    /**
     * called when service send a message indicating the song list has been sent
     * @param bundle song list info
     */
    protected void songListReceived(Bundle bundle){

    }

    /**
     * the service has been finished
     */
    protected void finishingService(){

    }


    /**
     * play-pause button clicked
     * @param v
     */
    public void onPlayPauseClicked(View v){
        LogHelper.d(TAG, "play-pause clicked");
        switch (currentSongState) {
            case STATE_STOPPED:
            case STATE_PAUSED:
                sendResumeMessageToService();
                break;
            case STATE_PLAYING:
                sendPauseMessageToService();
                break;
            default:
                break;
        }
    }

    /**
     * prev button clicked
     */
    public void onPrevClicked(View v){
        LogHelper.d(TAG, "prev clicked");
        sendSkipToPrevMessageToService();
    }

    /**
     * next button clicked
     */
    public void onNextClicked(View v){
        LogHelper.d(TAG, "next clicked");
        sendSkipToNextMessageToService();
    }


}
