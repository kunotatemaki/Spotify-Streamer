package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MusicServiceActivity extends ToolbarActivity {

    //protected MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    Messenger mService = null;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int currentSong = 0;
            switch (msg.what) {
                case MusicService.MSG_PAUSED_SONG:
                    currentSong = msg.arg1;
                    pausedSong(currentSong);
                    break;
                case MusicService.MSG_PLAYING_SONG:
                    currentSong = msg.arg1;
                    playingSong(currentSong);
                    break;
                case MusicService.MSG_FINISHED_PLAYING_SONG:
                    currentSong = msg.arg1;
                    finishedPlayingSong(currentSong);
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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, getApplicationContext().BIND_AUTO_CREATE);
            startService(playIntent);
        }
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

    protected void sendSetAsForegroundMessageToService() {
        if (musicBound && mService != null) {
            try {
                Message msg = Message.obtain(null, MusicService.MSG_SET_AS_FOREGROUND);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
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

    protected void sendSetSongListMessageToService(List<ListItem> songs) {
        if (musicBound && mService != null) {
            try {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("list_of_songs", (ArrayList<ListItem>)songs);
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

    protected void pausedSong(int currentSong){

    }

    protected void playingSong(int currentSong){

    }

    protected void finishedPlayingSong(int currentSong){

    }


}
