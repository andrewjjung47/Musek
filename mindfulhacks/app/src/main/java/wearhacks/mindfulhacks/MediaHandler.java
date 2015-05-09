package wearhacks.mindfulhacks;

import android.content.Context;
import android.media.AudioManager;
import android.os.RemoteException;
import android.media.MediaPlayer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Class in charge of playing media. Usually a static instance.
 */
public class MediaHandler {

    static MediaPlayer musicPlayer;
    static MediaPlayer podcastPlayer;

    static public AudioManager am;

    static private int MAX_INT_VOLUME = 100;
    static private int MIN_INT_VOLUME = 0;
    static private float MAX_FLOAT_VOLUME = 1;
    static private float MIN_FLOAT_VOLUME = 0;

    static private int iVolume = 50;
    static private int cVolume;

    public MediaHandler(MainActivity mainActivity) {

        final MainActivity main = mainActivity;

        // Automatically plays local file uptownfunk in /res/raw
        musicPlayer = MediaPlayer.create(main.getApplicationContext(), R.raw.uptownfunk);

        am = (AudioManager) main.getSystemService(Context.AUDIO_SERVICE);

    }

    static private void updateVolume(int change) {
        cVolume += change;

        // Clamp volume within valid ranges
        if (cVolume > MAX_INT_VOLUME) {
            cVolume = MAX_INT_VOLUME;
        }

        if (cVolume < MIN_INT_VOLUME) {
            cVolume = MIN_INT_VOLUME;
        }

        //convert to float value
        float fVolume = 1 - ((float) Math.log(MAX_INT_VOLUME - cVolume) / (float) Math.log(MAX_INT_VOLUME));

        if (fVolume > MAX_FLOAT_VOLUME) {
            fVolume = MAX_FLOAT_VOLUME;
        }

        if (fVolume < MIN_FLOAT_VOLUME) {
            fVolume = MIN_FLOAT_VOLUME;
        }

        musicPlayer.setVolume(fVolume, fVolume);
    }

    static public boolean isPlaying(){

        return musicPlayer.isPlaying();

    }

    static public void fadeOut() {
        float fadeDuration = 5;

        iVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        cVolume = iVolume;

        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                updateVolume(-1);
                if (cVolume == MIN_INT_VOLUME)
                {
                    timer.cancel();
                    timer.purge();
                    musicPlayer.pause();
                }
            }
        };

        // calculate delay, cannot be zero, set to 1 if zero
        int delay = (int) fadeDuration/iVolume;
        if (delay == 0) delay = 1;

        timer.schedule(timerTask, delay, delay);
    }

    static public void fadeIn() {
        float fadeDuration = 5;

        iVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        cVolume = iVolume;

        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                updateVolume(1);
                if (cVolume == MAX_INT_VOLUME)
                {
                    timer.cancel();
                    timer.purge();
                    musicPlayer.start();
                }
            }
        };

        // calculate delay, cannot be zero, set to 1 if zero
        int delay = (int) fadeDuration/iVolume;
        if (delay == 0) delay = 1;

        timer.schedule(timerTask, delay, delay);
    }
}
