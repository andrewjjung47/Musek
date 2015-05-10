package wearhacks.mindfulhacks;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrewjj on 09/05/15.
 */
public class MindfulMuse {
    final WeakReference<Activity> activityRef;
    private Muse muse = null;
    private ConnectionListener connectionListener = null;
    private DataListener dataListener = null;
    private boolean dataTransmission = true;
    private MuseFileWriter fileWriter = null;
    private MindfulMuse.UserState userState = MindfulMuse.UserState.LOLO;
    private Double mellow = 0.0;
    private Double conc = 0.0;
    public String oldState = "unknown";
    public MediaHandler mh;

    public MindfulMuse(final MainActivity main, File file) {
        mh = main.mediaHandler;
        WeakReference<Activity> activityRef =
                new WeakReference<Activity>(main);

        this.activityRef = activityRef;
        connectionListener = new ConnectionListener();
        dataListener = new DataListener(activityRef);

        fileWriter = MuseFileWriterFactory.getMuseFileWriter(file);
        Log.i("Muse Headband", "libmuse version=" + LibMuseVersion.SDK_VERSION);
        fileWriter.addAnnotationString(1, "MainActivity onCreate");
        dataListener.setFileWriter(fileWriter);
    }

    /**
     * Connection listener updates UI with new connection status and logs it.
     */
    class ConnectionListener extends MuseConnectionListener {
        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
            final ConnectionState current = p.getCurrentConnectionState();
            final String status = p.getPreviousConnectionState().toString() +
                    " -> " + current;
            final String full = "Muse " + p.getSource().getMacAddress() +
                    " " + status;
            Log.i("Muse Headband", full);
        }
    }

    class DataListener extends MuseDataListener {
        final WeakReference<Activity> activityRef;

        DataListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        private MuseFileWriter fileWriter;

        @Override
        public void receiveMuseDataPacket(MuseDataPacket p) {
            switch (p.getPacketType()) {
                case ALPHA_RELATIVE:
                    updateAlphaRelative(p.getValues());
                    break;
                case BATTERY:
                    fileWriter.addDataPacket(1, p);
                    // It's library client responsibility to flush the buffer,
                    // otherwise you may get memory overflow.
                    if (fileWriter.getBufferedMessagesSize() > 8096)
                        fileWriter.flush();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
            if (p.getHeadbandOn() && p.getBlink()) {
                Log.i("Artifacts", "blink");
            }
        }

        private void updateAlphaRelative(final ArrayList<Double> data) {
            final Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView elem1 = (TextView) activity.findViewById(R.id.elem1);
                        TextView elem2 = (TextView) activity.findViewById(R.id.elem2);
                        TextView elem3 = (TextView) activity.findViewById(R.id.elem3);
                        TextView elem4 = (TextView) activity.findViewById(R.id.elem4);
                        elem1.setText(String.format(
                                "%6.2f", data.get(Eeg.TP9.ordinal())));
                        elem2.setText(String.format(
                                "%6.2f", data.get(Eeg.FP1.ordinal())));
                        elem3.setText(String.format(
                                "%6.2f", data.get(Eeg.FP2.ordinal())));
                        elem4.setText(String.format(
                                "%6.2f", data.get(Eeg.TP10.ordinal())));
                    }
                });
            }
        }

        private void updateUserState(final ArrayList<Double> data, String dataType) {
            if (dataType == "mellow") {
                mellow = data.get(0);
            }
            else if (dataType == "conc"){
                conc = data.get(0);
            }

            if (mellow < 0.5 && conc < 0.5) {
                userState = MindfulMuse.UserState.LOLO;
            }
            else if (mellow < 0.5 & conc >= 0.5) {
                userState = MindfulMuse.UserState.LOHI;
            }
            else if (mellow >= 0.5 & conc < 0.5) {
                userState = MindfulMuse.UserState.HILO;
            }
            else if (mellow >= 0.5 & conc >= 0.5) {
                userState = MindfulMuse.UserState.HIHI;
            }

            final Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView state = (TextView) activity.findViewById(R.id.userState);
                        state.setText(userState.toString());
                        String currentState = userState.toString();
                        if (currentState != oldState) {
                            mh.updateMusic(currentState);
                            oldState = currentState;
                        }
                    }
                });
            }
        }

        public void setFileWriter(MuseFileWriter fileWriter) {
            this.fileWriter  = fileWriter;
        }
    }

    public void refresh() {
        // Refresh
        MuseManager.refreshPairedMuses();
        List<Muse> pairedMuses = MuseManager.getPairedMuses();
        for (Muse m: pairedMuses) {
            String dev_id = m.getName() + "-" + m.getMacAddress();
            Log.i("Muse Headband", dev_id);
        }
    }

    public void connect() {
        // Refresh
        MuseManager.refreshPairedMuses();
        List<Muse> pairedMuses = MuseManager.getPairedMuses();
        for (Muse m: pairedMuses) {
            String dev_id = m.getName() + "-" + m.getMacAddress();
            Log.i("Muse Headband", dev_id);
        }

        // Connect
        if (pairedMuses.size() < 1) {
            Log.w("Muse Headband", "There is nothing to connect to");
        }
        else {
            muse = pairedMuses.get(0);
            ConnectionState state = muse.getConnectionState();
            if (state == ConnectionState.CONNECTED ||
                    state == ConnectionState.CONNECTING) {
                Log.w("Muse Headband", "doesn't make sense to connect second time to the same muse");
                return;
            }
            configure_library();
            fileWriter.open();
            fileWriter.addAnnotationString(1, "Connect clicked");
            /**
             * In most cases libmuse native library takes care about
             * exceptions and recovery mechanism, but native code still
             * may throw in some unexpected situations (like bad bluetooth
             * connection). Print all exceptions here.
             */
            try {
                muse.runAsynchronously();
            } catch (Exception e) {
                Log.e("Muse Headband", e.toString());
            }
        }
    }

    public void disconnect() {
        if (muse != null) {
            /**
             * true flag will force libmuse to unregister all listeners,
             * BUT AFTER disconnecting and sending disconnection event.
             * If you don't want to receive disconnection event (for ex.
             * you call disconnect when application is closed), then
             * unregister listeners first and then call disconnect:
             * muse.unregisterAllListeners();
             * muse.disconnect(false);
             */
            muse.disconnect(true);
            fileWriter.addAnnotationString(1, "Disconnect clicked");
            fileWriter.flush();
            fileWriter.close();
        }
    }

    private void configure_library() {
        muse.registerConnectionListener(connectionListener);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ACCELEROMETER);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.EEG);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ALPHA_RELATIVE);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ARTIFACTS);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.BATTERY);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.CONCENTRATION);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.MELLOW);
        muse.setPreset(MusePreset.PRESET_14);
        muse.enableDataTransmission(dataTransmission);
    }

    public static enum UserState {
        LOLO {
            public String toString() {
                return "Relaxed";
            }
        },
        LOHI {
            public String toString() {
                return "Focused";
            }
        },
        HILO {
            public String toString() {
                return "Alert";
            }
        },
        HIHI {
            public String toString() {
                return "Excited";
            }
        }
    }
}
