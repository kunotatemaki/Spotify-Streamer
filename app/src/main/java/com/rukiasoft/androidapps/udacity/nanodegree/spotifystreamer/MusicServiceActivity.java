package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

public class MusicServiceActivity extends ToolbarAndRefreshActivity {

    static final int STATE_PLAYING = 1;
    static final int STATE_PAUSED = 2;
    static final int STATE_STOPED = 3;
    static final int STATE_BUFFERING = 4;
    private static final String STATE_ACTIVITY = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.musicserviceactivity.stateactivity";
    protected Boolean MusicServiceActivityVisible;

    //protected MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
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
            //MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            //musicSrv = binder.getService();
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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVITY)){
            currentSongState = savedInstanceState.getInt(STATE_ACTIVITY);
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
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onDestroy() {
        //stopService(playIntent);
        //musicSrv=null;
        unbindService(musicConnection);
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
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();
        MusicServiceActivityVisible = true;
    }

    @Override
    public void onPause(){
        super.onPause();
        MusicServiceActivityVisible = false;
    }

    protected void sendPlayMessageToService() {
        if (musicBound && mService != null) {
            try {
                Message msg = Message.obtain(null, MusicService.MSG_PLAY);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendPauseMessageToService() {
        if (musicBound && mService != null) {
            try {
                Message msg = Message.obtain(null, MusicService.MSG_PAUSE);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendResumeMessageToService() {
        if (musicBound && mService != null) {
            try {
                Message msg = Message.obtain(null, MusicService.MSG_RESUME);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendSkipToNextMessageToService() {
        if (musicBound && mService != null) {
            try {
                Message msg = Message.obtain(null, MusicService.MSG_NEXT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendSkipToPrevMessageToService() {
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction( MusicService.ACTION_PREVIOUS );
        startService(intent);
        //TODO hacer lo mismo para el resto de botones

        /*PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        if (musicBound && mService != null) {
            try {
                Message msg = Message.obtain(null, MusicService.MSG_PREV);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }*/
    }

    protected void sendSetCurrentSongMessageToService(int position) {
        if (musicBound && mService != null) {
            try {
                Message msg = Message.obtain(null, MusicService.MSG_SET_CURRENT_SONG, position, 0);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendSeekToPosition(int mseconds) {
        if (musicBound && mService != null) {
            try {
                Message msg = Message.obtain(null, MusicService.MSG_SEEK_TO_EXACT_POSITION, mseconds, 0);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendSetSongListMessageToService(List<ListItem> songs, String id) {
        if (musicBound && mService != null) {
            try {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(MusicService.SONG_LIST, (ArrayList<ListItem>)songs);
                bundle.putString(MusicService.ARTIST_ID, id);
                Message msg = Message.obtain(null, MusicService.MSG_SET_SONG_LIST);
                msg.setData(bundle);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendAskForCurrentPlayingSongService(){
        if (musicBound && mService != null) {
            try {
                Message msg = Message.obtain(null, MusicService.MSG_ASK_CURRENT_PLAYING_SONG);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendAskForCurrentListService(){
        if (musicBound && mService != null) {
            try {
                Message msg = Message.obtain(null, MusicService.MSG_ASK_CURRENT_LIST);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void pausedSong(Bundle bundle){
        currentSongState = STATE_PAUSED;
        hideRefreshLayoutSwipeProgress();
    }

    protected void playingSong(Bundle bundle){
        currentSongState = STATE_PLAYING;
        hideRefreshLayoutSwipeProgress();
    }

    protected void finishedPlayingSong(Bundle bundle){
        currentSongState = STATE_STOPED;
        hideRefreshLayoutSwipeProgress();
    }

    protected void bufferingSong(){
        currentSongState = STATE_BUFFERING;
        showRefreshLayoutSwipeProgress();
    }

    protected void seekBarPositionReceived(int position){

    }

    protected void songListReceived(Bundle bundle){

    }





}
