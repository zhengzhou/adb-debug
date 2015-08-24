package siir.es.adbWireless;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * adb workThread.
 * Created by xu on 2015/8/24 0024.
 */
public class AdbService extends IntentService {

    public static final String EXTRA_ACTION_START= BuildConfig.APPLICATION_ID + ".EXTRA_ACTION_START";
    public static final String EXTRA_ACTION_STOP = BuildConfig.APPLICATION_ID + ".EXTRA_ACTION_STOP";

    private static boolean USB_DEBUG = false;

    public static class ADBEvent{

        private String mAction;
        private boolean mSuccess;

        public ADBEvent(String action, boolean success) {
            this.mAction = action;
            this.mSuccess = success;
        }

        public boolean isSuccess() {
            return mSuccess;
        }
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public AdbService() {
        super("adb service thread");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean result = false;
        String action = intent.getAction();
        if(EXTRA_ACTION_START.equals(action)){
            result = adbStart(this);
        }else if(EXTRA_ACTION_STOP.equals(action)){
            result = adbStop(this);
        }
        BusPro.post(new ADBEvent(action, result));
    }

    boolean adbStart(Context context) {
        try {
            if (!USB_DEBUG) {
                Utils.setProp("service.adb.tcp.port", MainActivity.PORT);
                try {
                    if (Utils.isProcessRunning("adbd")) {
                        Utils.runRootCommand("stop adbd");
                    }
                } catch (Exception e) {
                    //no-op
                }
                Utils.runRootCommand("start adbd");
            }
            try {
                MainActivity.mState = true;
            } catch (Exception e) {
            }
            SharedPreferences settings = context.getSharedPreferences("wireless", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("mState", true);
            editor.apply();

            // Try to auto connect
            if (Utils.prefsAutoCon(context)) {
                Utils.autoConnect(context, "c");
            }


            if (Utils.prefsNoti(context)) {
                Utils.showNotification(context, R.drawable.ic_stat_adbwireless, context.getString(R.string.noti_text) + " " + Utils.getWifiIp(context));
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    boolean adbStop(Context context) {
        try {
            if (!MainActivity.USB_DEBUG) {
                if (MainActivity.mState) {
                    Utils.setProp("service.adb.tcp.port", "-1");
                    Utils.runRootCommand("stop adbd");
                    Utils.runRootCommand("start adbd");
                }
            }
            MainActivity.mState = false;

            SharedPreferences settings = context.getSharedPreferences("wireless", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("mState", false);
            editor.apply();

            // Try to auto disconnect
            if (Utils.prefsAutoCon(context)) {
                Utils.autoConnect(context, "d");
            }

            if (Utils.mNotificationManager != null) {
                Utils.mNotificationManager.cancelAll();
            }
        } catch (Exception e) {
            return false;
        }
        return true;

    }

}
