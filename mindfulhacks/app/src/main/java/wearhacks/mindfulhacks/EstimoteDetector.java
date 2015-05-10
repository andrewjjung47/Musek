package wearhacks.mindfulhacks;

import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Class in charge of interactions with the estimote beacons
 */
public class EstimoteDetector {

    private BeaconManager beaconManager;

    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

    public EstimoteDetector(MainActivity mainActivity) {

        final MainActivity main = mainActivity;

        // Initializing beacon, region, and manager to connect to estimote
        ArrayList<Beacon> beacons = new ArrayList<Beacon>();
        beacons.add(new Beacon("b9407f30-f5f8-466e-aff9-25556b57fe6d",
                "estimote",
                "E5:3D:D0:63:FD:88",
                64904, 53347,
                -74, -62));

        beacons.add(new Beacon("b9407f30-f5f8-466e-aff9-25556b57fe6d",
                "estimote",
                "E5:3D:D0:63:FD:88",
                10470, 33680,
                -74, -41));

        beaconManager = new BeaconManager(main);

        // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
        // In order for this demo to be more responsive and immediate we lower down those values.
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);

        final ArrayList<Beacon> beaconList = new ArrayList<Beacon>(beacons);

        // Configure BeaconManager.
        beaconManager = new BeaconManager(main);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                for (Beacon rangedBeacon : beacons) {
                    if (rangedBeacon.getMajor() == beaconList.get(0).getMajor()) {
                        double distance = Math.min(Utils.computeAccuracy(rangedBeacon), 6.0);
//                        TextView tv = (TextView) main.findViewById(R.id.TV1);
//                        tv.setText(String.valueOf(distance));

                        if (distance < 2.0) {
                            main.mediaHandler.activatePodcast(true);
                        } else {
                            main.mediaHandler.activatePodcast(false);
                        }
                    }
                }
            }
        });

    }

    public void connect() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    Log.e("Connect", "Cannot start ranging", e);
                }
            }
        });
    }

}
