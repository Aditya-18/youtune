package com.example.admin.gui;

/**
 * Created by ADMIN on 19-01-2017.
 */
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;


public class TunnelPlayerWorkaround {
    private static final String TAG = "TunnelPlayerWorkaround";

    private static final String SYSTEM_PROP_TUNNEL_DECODE_ENABLED = "tunnel.decode";

    private TunnelPlayerWorkaround()
    {
    }
    public static boolean isTunnelDecodeEnabled(Context context)
    {
        return SystemPropertiesProxy.getBoolean(context, SYSTEM_PROP_TUNNEL_DECODE_ENABLED, false);
    }
    public static MediaPlayer createSilentMediaPlayer(Context context)
    {
        boolean result = false;

        MediaPlayer mp = null;
        try {
            mp = MediaPlayer.create(context, Uri.parse("/sdcard/wav/test3.wav"));
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            result = true;
            } catch (RuntimeException e) {
                  Log.e(TAG, "createSilentMediaPlayer()", e);
            } finally {
            if (!result && mp != null) {
            try {
                mp.release();
                } catch (IllegalStateException e) {
            }
            }
        }
        return mp;
        }
}