package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MusicIntentService extends IntentService  implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener {

    private static final String ACTION_PLAY = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.action.PLAY";

    private static final String LIST_PARAM = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.extra.LIST";
    private static final String TRACK_PARAM = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.extra.SELECTED_TRACK";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, MusicIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }



    public MusicIntentService() {
        super("MusicIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}


//http://stackoverflow.com/questions/17742477/mediaplayer-progress-update-to-seekbar-not-smooth
//http://stackoverflow.com/questions/9088315/start-intentservice-from-activity-and-refresh-activity-when-intentservice-is-fin