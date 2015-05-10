package wearhacks.mindfulhacks;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;

import android.app.Activity;
import android.os.Environment;
import android.view.View.OnClickListener;
import android.widget.Spinner;

public class MainActivity extends Activity implements OnClickListener {

    MediaHandler mediaHandler;

    private EstimoteDetector estimoteDetector;
    private MindfulMuse mindfulMuse;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button connectButton = (Button) findViewById(R.id.connect);
        connectButton.setOnClickListener(this);
        Button disconnectButton = (Button) findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(this);

        initialize();
    }

    private void initialize() {


        Button musicBtn = (Button) findViewById(R.id.btn2);

        musicBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                debug();
            }

        });

        // Creates new EstimoteDetector class
        estimoteDetector = new EstimoteDetector(this);

        mediaHandler = new MediaHandler(this);

        mindfulMuse = new MindfulMuse(this, new File(
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "testlibmusefile.muse"));

        Button btn1 = (Button) findViewById(R.id.connect);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mindfulMuse.connect();
                connect();
            }
        });

        Button btn2 = (Button) findViewById(R.id.disconnect);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mindfulMuse.disconnect();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaHandler.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaHandler.start();
    }

    @Override
    public void onClick(View v) {
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

        if (MediaHandler.isPlaying()) {
            MediaHandler.pause();
        } else {
            MediaHandler.toggleMusic = true;
            MediaHandler.start();
        }
    }
}
