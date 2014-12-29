package fr.oxilea.btlocate;

import android.app.Activity;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.prefs.Preferences;


public class MyRegisterActivity extends Activity {

    private BluetoothAdapter mBluetoothAdapter;

    private boolean mScanning;
    private Handler mHandler = new Handler();

    // request to start BT
    private static final int REQUEST_ENABLE_BT = 1;
    private static final Boolean START_SCAN = true;
    private static final Boolean STOP_SCAN = false;

    //  Scan period 60 seconds.
    private static final long SCAN_PERIOD = 60000;

    DeviceArray myDeviceArray = new DeviceArray();
    BluetoothGatt mBluetoothGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScanning=false;

        setContentView(R.layout.activity_my_register);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_register, menu);
        return true;
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // this is the answer from start BT
                if (resultCode != RESULT_OK) {
                    // could not search BT devices BT nor activated
                    Toast.makeText(this, R.string.search_stop_bt_not_activated, Toast.LENGTH_SHORT).show();
                }
                return;
        }
        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void registerUnlockDevice(View v) {
        String unlockDevAdd=null;

        // get the line click (corresponding text id)
        switch (v.getId()){

            case R.id.device1:
                unlockDevAdd = myDeviceArray.getDeviceAdd(0);
                break;

            case R.id.device2:
                unlockDevAdd = myDeviceArray.getDeviceAdd(1);
                break;

            case R.id.device3:
                unlockDevAdd = myDeviceArray.getDeviceAdd(2);
                break;

            case R.id.device4:
                unlockDevAdd = myDeviceArray.getDeviceAdd(3);
                break;

            case R.id.device5:
                unlockDevAdd = myDeviceArray.getDeviceAdd(4);
                break;
        }


        if ((unlockDevAdd!=null) && (unlockDevAdd!="")) {

            // Display feedback to the user, ie the device set
            String deviceSet = "Device:  " + unlockDevAdd + " set";
            Toast.makeText(this, deviceSet, Toast.LENGTH_SHORT).show();

            // save the MAC address of the device
            SharedPreferences settings = getPreferences(0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("Unlock Device Address", unlockDevAdd);

            // Commit the edits!
            editor.commit();

            keyguardUnlock();
            // start the service
        }

    }


    public void keyguardUnlock()
    {
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }



    public void launchSearch(View v)
    {
        // check if BT LE Supported
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }
        else{
            // launch BT LE search device
            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            mBluetoothAdapter = bluetoothManager.getAdapter();

            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else
            {
                if (mScanning)
                {
                    // currently scanning: switch off
                    scanLeDevice(STOP_SCAN);

                }
                else
                {
                    // BT started try to search devices
                    scanLeDevice(START_SCAN);
                }

            }
        }

    }


    // start / Stop Scan
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    mScanning = false;

                    // display start button
                    Button myButton = (Button) findViewById(R.id.searchButton);
                    myButton.setText(R.string.start_search_device_str);
                }
            }, SCAN_PERIOD);

            mScanning = true;

            // display stop button
            Button myButton = (Button) findViewById(R.id.searchButton);
            myButton.setText(R.string.stop_search_device_str);

            // clear current device list
            myDeviceArray.clearAllDevices();

            // start BT scan
            mBluetoothAdapter.startLeScan(mLeScanCallback);

            // update screen CB
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        while (!isInterrupted()) {
                            Thread.sleep(1000);

                            // stop scan has been requested, stop one second refresh too
                            if (mScanning == false)
                                interrupt();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // update TextView here!
                                    TextView textView = (TextView)findViewById(R.id.device1);
                                    textView.setText(myDeviceArray.getDevice(0));

                                    textView = (TextView)findViewById(R.id.device2);
                                    textView.setText(myDeviceArray.getDevice(1));

                                    textView = (TextView)findViewById(R.id.device3);
                                    textView.setText(myDeviceArray.getDevice(2));

                                    textView = (TextView)findViewById(R.id.device4);
                                    textView.setText(myDeviceArray.getDevice(3));

                                    textView = (TextView)findViewById(R.id.device5);
                                    textView.setText(myDeviceArray.getDevice(4));
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                    }
                }
            };

            t.start();


        } else {
            mScanning = false;

            // display start button
            Button myButton = (Button) findViewById(R.id.searchButton);
            myButton.setText(R.string.start_search_device_str);

            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {

                    myDeviceArray.addDevice(device, rssi);
                }
            };


}
