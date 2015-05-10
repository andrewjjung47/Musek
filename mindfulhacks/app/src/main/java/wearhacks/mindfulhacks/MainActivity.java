package wearhacks.mindfulhacks;

import android.media.AudioManager;
import android.os.RemoteException;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.estimote.sdk.cloud.internal.User;
import com.interaxon.libmuse.Accelerometer;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.LibMuseVersion;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseFileWriter;
import com.interaxon.libmuse.MuseFileWriterFactory;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;
import com.interaxon.libmuse.MuseVersion;

import wearhacks.mindfulhacks.MindfulMuse;

import com.estimote.sdk.Beacon;

public class MainActivity extends Activity implements OnClickListener {

    MediaPlayer mediaPlayer;

    private EstimoteDetector estimoteDetector;
    private MindfulMuse mindfulMuse;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button refreshButton = (Button) findViewById(R.id.refresh);
        refreshButton.setOnClickListener(this);
        Button connectButton = (Button) findViewById(R.id.connect);
        connectButton.setOnClickListener(this);
        Button disconnectButton = (Button) findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(this);


        initialize();
    }

    private void initialize() {
        Button btn = (Button) findViewById(R.id.btn1);

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                connect();
            }

        });

        btn = (Button) findViewById(R.id.btn2);

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                debug();
            }

        });

        // Creates new EstimoteDetector class
        estimoteDetector = new EstimoteDetector(this);


//        String url = "http://podcast.cbc.ca/mp3/podcasts/current_20150508_81750.mp3"; // your URL here
//        MediaPlayer mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        try {
//            mediaPlayer.setDataSource(url);
//            mediaPlayer.prepare(); // might take long! (for buffering, etc)
//            mediaPlayer.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        // Automatically plays local file uptownfunk in /res/raw
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.uptownfunk);
//        mediaPlayer.start(); // no need to call prepare(); create() does that for you

        mindfulMuse = new MindfulMuse(this, new File(
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "testlibmusefile.muse"));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        Spinner musesSpinner = (Spinner) findViewById(R.id.muses_spinner);
        mindfulMuse.processInput(v, musesSpinner, this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void connect() {
        Log.v("dbg", "Connect");

        estimoteDetector.connect();

    }

    private void debug() {
        Log.v("dbg", "Debug");

        if (mediaPlayer.isPlaying()) {
            //mediaPlayer.pause();
            fadeOut();
        } else {
            //mediaPlayer.start();
            fadeIn();
        }
    }

    private int MAX_INT_VOLUME = 100;
    private int MIN_INT_VOLUME = 0;
    private float MAX_FLOAT_VOLUME = 1;
    private float MIN_FLOAT_VOLUME = 0;

    private int iVolume = 50;
    private int cVolume;

    private void updateVolume(int change) {
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

        mediaPlayer.setVolume(fVolume, fVolume);
    }

    private void fadeOut() {
        float fadeDuration = 5;

        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
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
                    mediaPlayer.pause();
                }
            }
        };

        // calculate delay, cannot be zero, set to 1 if zero
        int delay = (int) fadeDuration/iVolume;
        if (delay == 0) delay = 1;

        timer.schedule(timerTask, delay, delay);
    }

    private void fadeIn() {
        float fadeDuration = 5;

        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
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
                    mediaPlayer.start();
                }
            }
        };

        // calculate delay, cannot be zero, set to 1 if zero
        int delay = (int) fadeDuration/iVolume;
        if (delay == 0) delay = 1;

        timer.schedule(timerTask, delay, delay);
    }
}
