package siir.es.adbWireless;


import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.dd.CircularProgressButton;

public class MainActivity extends AppCompatActivity {

    public static final String PORT = "5555";
    public static final boolean USB_DEBUG = false;

    public static boolean mState = false;
    public static boolean wifiState;

    private TextView tv_footer_1;
    private TextView tv_footer_2;
    private TextView tv_footer_3;
    private ImageView iv_button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initView();
        if (Utils.mNotificationManager == null) {
            Utils.mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        if (!Utils.hasRootPermission()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.no_root)).setCancelable(true).setPositiveButton(getString(R.string.button_close), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    MainActivity.this.finish();
                }
            });
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.create();
            builder.setTitle(R.string.no_root_title);
            builder.show();
        }

        wifiState = Utils.checkWifiState(this);
            Utils.saveWiFiState(this, wifiState);
        if (!wifiState) {
            if (Utils.prefsWiFiOn(this)) {
                Utils.enableWiFi(this, true);
            } else {
                Utils.WiFiDialog(this);
            }
        }

        this.iv_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (Utils.prefsHaptic(MainActivity.this))
                    vib.vibrate(45);
                try {
                    if (!mState) {
                        Utils.adbStart(MainActivity.this);
                    } else {
                        Utils.adbStop(MainActivity.this);
                    }
                    updateState();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        final CircularProgressButton circularButton = (CircularProgressButton) findViewById(R.id.circularButton);
        circularButton.setIndeterminateProgressMode(true);
        circularButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circularButton.getProgress() == 0) {
                    circularButton.setProgress(50);
                } else if (circularButton.getProgress() == -1) {
                    circularButton.setProgress(0);
                } else {
                    circularButton.setProgress(-1);
                }
            }
        });

    }

    private void initView() {
        this.iv_button = (ImageView) findViewById(R.id.iv_button);
        this.tv_footer_1 = (TextView) findViewById(R.id.tv_footer_1);
        this.tv_footer_2 = (TextView) findViewById(R.id.tv_footer_2);
        this.tv_footer_3 = (TextView) findViewById(R.id.tv_footer_3);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences("wireless", 0);
        mState = settings.getBoolean("mState", false);
        wifiState = settings.getBoolean("wifiState", false);
        updateState();
    }

    @Override
    protected void onDestroy() {
        try {
            Utils.adbStop(this);
            Utils.mNotificationManager.cancelAll();
        } catch (Exception e) {
            //no-op
        }
        /*
		 * try { if(Utils.mWakeLock != null) { Utils.mWakeLock.release(); } } catch (Exception e) { }
		 */
        try {
            if (Utils.prefsWiFiOff(this) && !wifiState && Utils.checkWifiState(this)) {
                Utils.enableWiFi(this, false);
            }
        } catch (Exception e) {
            //no-op
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    /*public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_prefs:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, Utils.ACTIVITY_SETTINGS);
                break;
            case R.id.menu_about:
                this.showHelpDialog();
                return true;
            case R.id.menu_exit:
                showExitDialog();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }*/


    private void updateState() {
        if (mState) {
            tv_footer_1.setText(R.string.footer_text_1);
            try {
                tv_footer_2.setText("adb connect " + Utils.getWifiIp(this));
            } catch (Exception e) {
                tv_footer_2.setText("adb connect ?");
            }
            tv_footer_2.setVisibility(View.VISIBLE);
            tv_footer_3.setVisibility(View.VISIBLE);
            iv_button.setImageResource(R.drawable.bt_on);
        } else {
            tv_footer_1.setText(R.string.footer_text_off);
            tv_footer_2.setVisibility(View.INVISIBLE);
            tv_footer_3.setVisibility(View.INVISIBLE);
            iv_button.setImageResource(R.drawable.bt_off);
        }
    }

}