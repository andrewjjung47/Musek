package wearhacks.mindfulhacks;

import android.content.Context;
import android.media.AudioManager;
import android.os.RemoteException;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class in charge of playing media. Usually a static instance.
 */
public class MediaHandler {

    public String brainState;

    static public MediaPlayer musicPlayer;
    static public MediaPlayer podcastPlayer;

    // Are true if we are pausing the MediaPlayer and fading out afterwards
    static public boolean toggleMusic = false;
    static public boolean togglePodcast = false;

    static public AudioManager am;

    static private int MAX_INT_VOLUME = 100;
    static private int MIN_INT_VOLUME = 0;
    static private float MAX_FLOAT_VOLUME = 1;
    static private float MIN_FLOAT_VOLUME = 0;

    static private int iVolume = 50;
    static private int cVolume;

    final MainActivity main;

    public MediaHandler(MainActivity mainActivity) {

        main = mainActivity;

        // Automatically plays local file uptownfunk in /res/raw
        musicPlayer = MediaPlayer.create(main.getApplicationContext(), R.raw.relaxed);

        String url = "http://podcast.cbc.ca/mp3/podcasts/current_20150508_81750.mp3"; // your URL here
        podcastPlayer = new MediaPlayer();
        podcastPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            podcastPlayer.setDataSource(url);
            podcastPlayer.prepare(); // might take long! (for buffering, etc)
            //podcastPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        if (toggleMusic) {
            musicPlayer.setVolume(fVolume, fVolume);
        }
        if (togglePodcast) {
            podcastPlayer.setVolume(fVolume, fVolume);
        }
    }

    static public boolean isPlaying() {
        return (musicPlayer.isPlaying() | podcastPlayer.isPlaying());
    }

    public void updateMusic(String state) {
        if (musicPlayer.isPlaying()) {
            musicPlayer.stop();
            musicPlayer.release();
        }
        if (state == "Relaxed") {
            musicPlayer = MediaPlayer.create(main.getApplicationContext(), R.raw.relaxed);
        } else if (state == "Focused") {
            musicPlayer = MediaPlayer.create(main.getApplicationContext(), R.raw.focused);
        } else if (state == "Alert") {
            musicPlayer = MediaPlayer.create(main.getApplicationContext(), R.raw.alert);
        } else {
            musicPlayer = MediaPlayer.create(main.getApplicationContext(), R.raw.excited);
        }
        musicPlayer.start();
    }

    static public void pause() {

        if (musicPlayer.isPlaying()) {
            toggleMusic = true;
            musicPlayer.pause();
        }
        if (podcastPlayer.isPlaying()) {
            togglePodcast = true;
            podcastPlayer.pause();
        }
    }

    static public void start() {

        if (toggleMusic == true) {
            musicPlayer.start();
            toggleMusic = false;
        }
        if (togglePodcast == true) {
            podcastPlayer.start();
            togglePodcast = false;
        }
    }

    static public boolean isMusicPlaying() {

        return musicPlayer.isPlaying();

    }

    static public boolean isPodcastPlaying() {

        return podcastPlayer.isPlaying();

    }

    static public void fadeOut() {

        //if (musicPlayer.isPlaying()) {
        //    toggleMusic = true;
        //}
        //if (podcastPlayer.isPlaying()) {
        //    togglePodcast = true;
        //}

        float fadeDuration = 5;

        iVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        cVolume = iVolume;

        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                updateVolume(-1);
                if (cVolume == MIN_INT_VOLUME) {
                    timer.cancel();
                    timer.purge();
                    if (toggleMusic) {
                        musicPlayer.pause();
                    }
                    if (togglePodcast) {
                        podcastPlayer.pause();
                    }
                }
            }
        };

        // calculate delay, cannot be zero, set to 1 if zero
        int delay = (int) fadeDuration / iVolume;
        if (delay == 0) delay = 1;

        timer.schedule(timerTask, delay, delay);
    }

    static public void fadeIn() {
        float fadeDuration = 5;

        iVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        cVolume = iVolume;

        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                updateVolume(1);
                if (cVolume == MAX_INT_VOLUME) {
                    timer.cancel();
                    timer.purge();
                    if (toggleMusic) {
                        musicPlayer.start();
                        toggleMusic = false;
                    }
                    if (togglePodcast) {
                        podcastPlayer.start();
                        togglePodcast = false;
                    }
                }
            }
        };

        // calculate delay, cannot be zero, set to 1 if zero
        int delay = (int) fadeDuration / iVolume;
        if (delay == 0) delay = 1;

        timer.schedule(timerTask, delay, delay);
    }

    public void activatePodcast(boolean podcastactive) {
        togglePodcast = podcastactive;
        if (togglePodcast == true) {
            // we need to activate podcast
            if (musicPlayer.isPlaying() == true) {
                TextView brainmood = (TextView) main.findViewById(R.id.brainmood);
                brainmood.setText("Room:");
                TextView userState = (TextView) main.findViewById(R.id.userState);
                brainState = userState.getText().toString();
                userState.setText("Kitchen");

                musicPlayer.pause();
                podcastPlayer.start();
            }
        } else {
            // we need to deactivate podcast
            if (podcastPlayer.isPlaying() == true) {
                TextView brainmood = (TextView) main.findViewById(R.id.brainmood);
                brainmood.setText("Brain Mood:");
                TextView userState = (TextView) main.findViewById(R.id.userState);
                userState.setText(brainState);

                podcastPlayer.pause();
                musicPlayer.start();
            }
        }
    }
}