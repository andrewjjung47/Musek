package wearhacks.mindfulhacks;

import android.os.RemoteException;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.interaxon.libmuse.Accelerometer;
import com.estimote.sdk.Beacon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ActionBarActivity {

    private BeaconManager beaconManager;
    private ArrayList<Beacon> beacons;
    private ArrayList<Region> regions;

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Initializing beacon, region, and manager to connect to estimote
        beacons = new ArrayList<Beacon>();
        beacons.add(new Beacon( "b9407f30-f5f8-466e-aff9-25556b57fe6d",
                                "estimote",
                                "E5:3D:D0:63:FD:88",
                                64904, 53347,
                                -74, -62));

        beacons.add(new Beacon("b9407f30-f5f8-466e-aff9-25556b57fe6d",
                               "estimote",
                               "E5:3D:D0:63:FD:88",
                               10470, 33680,
                               -74, -41));

        regions = new ArrayList<Region>();
        regions.add(new Region("regionid0", beacons.get(0).getProximityUUID(), beacons.get(0).getMajor(), beacons.get(0).getMinor()));
        //regions.add(new Region("regionid1", beacons.get(1).getProximityUUID(), beacons.get(1).getMajor(), beacons.get(1).getMinor()));

        //regions.add(new Region("regionid", beacons.get(0).getProximityUUID()));

        beaconManager = new BeaconManager(this);

        // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
        // In order for this demo to be more responsive and immediate we lower down those values.
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);

        // Choose what to do when we enter/leave a region with a beacon
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> beacons) {
                for (Beacon rangedBeacon : beacons) {
                    if (rangedBeacon.getMacAddress().equals(beacons.get(0).getMacAddress())) {
                        Log.v("dbg", "In region of beacon 0");
                    } else if (rangedBeacon.getMacAddress().equals(beacons.get(0).getMacAddress())) {
                        Log.v("dbg", "In region of beacon 1");
                    }
                }
            }

            @Override
            public void onExitedRegion(Region region) {
                // Exit region, won't need to do anything really
                Log.v("dbg", "Left region");
            }
        });
//        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
//            @Override
//            public void onBeaconsDiscovered(Region region, final List<Beacon> rangedBeacons) {
//                // Note that results are not delivered on UI thread.
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // Just in case if there are multiple beacons with the same uuid, major, minor.
//                        Beacon foundBeacon = null;
//                        for (Beacon rangedBeacon : rangedBeacons) {
//                            if (rangedBeacon.getMacAddress().equals(beacons.get(0).getMacAddress())) {
//                                foundBeacon = rangedBeacon;
//                            } else if (rangedBeacon.getMacAddress().equals(beacons.get(0).getMacAddress())) {
//                                foundBeacon = rangedBeacon;
//                            }
//                        }
//                        if (foundBeacon != null) {
//                            if (foundBeacon.getMacAddress().equals(beacons.get(0).getMacAddress())) {
//                                double distance = Math.min(Utils.computeAccuracy(foundBeacon), 6.0);
//                                //updateDistanceView(foundBeacon);
//                                Log.v("dbg", distance + "");
//                                TextView tv = (TextView) findViewById(R.id.TV1);
//                                tv.setText(String.valueOf(distance));
//                            } else if (foundBeacon.getMacAddress().equals(beacons.get(0).getMacAddress())) {
//                                double distance = Math.min(Utils.computeAccuracy(foundBeacon), 6.0);
//                                //updateDistanceView(foundBeacon);
//                                Log.v("dbg", distance + "");
//                                TextView tv = (TextView) findViewById(R.id.TV2);
//                                tv.setText(String.valueOf(distance));
//                            }
//                        }
//                    }
//                });
//            }
//        });

        // Automatically plays local file uptownfunk in /res/raw
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.uptownfunk);
//        mediaPlayer.start(); // no need to call prepare(); create() does that for you
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

        // Connect to estimote
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
//                    beaconManager.startRanging(regions.get(0));
//                    beaconManager.startRanging(regions.get(1));
                    beaconManager.startMonitoring(regions.get(0));
                } catch (RemoteException e) {
                    Toast.makeText(MainActivity.this, "Cannot start ranging, something terrible happened",
                            Toast.LENGTH_LONG).show();
                    Log.e("dbg", "Cannot start ranging", e);
                }
            }
        });

    }

    private void debug() {
        Log.v("dbg", "Debug");

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }
}
