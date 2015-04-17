package bluetooth.aglogica.com.bluetoothtool;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import bluetooth.aglogica.com.bluetoothtool.service.AcquireDataThread;


public class MainActivity extends ActionBarActivity {

    // Debugging
    private static final String TAG = "MainActivity";

    // Message types sent from the Handler
    public static final int MESSAGE_SCREEN = 1;

    // Key names received from the Handler
    public static final String DEVICE_MESSAGE = "device_message";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private TextView textContext;
    private Button btnScanSensor;

    private StringBuilder mOutStringBuffer;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBluetooth();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            initialSetup();
        }
    }


    private void initBluetooth() {
        textContext = (TextView) findViewById(R.id.textContext);
        btnScanSensor = (Button) findViewById(R.id.btnInitSensor);
        mOutStringBuffer = new StringBuilder();

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
    }

    private void initialSetup() {
        btnScanSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textContext.setText("");
                mOutStringBuffer.setLength(0);
                Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
//                startActivityForResult(intent, REQUEST_CONNECT_DEVICE_SECURE);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE_INSECURE);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

//    private void ensureDiscoverable() {
//        if (mBluetoothAdapter.getScanMode() !=
//                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            startActivity(discoverableIntent);
//        }
//    }

    private final Handler subHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SCREEN:
                    mOutStringBuffer.append(msg.getData().getString(DEVICE_MESSAGE));
                    textContext.setText(mOutStringBuffer.toString());
                    break;
                default:
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
//                if (resultCode == Activity.RESULT_OK) {
//                    connectDevice(data, true);
//                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                connectDevice(data, false, resultCode);
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    initialSetup();
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure, int commandType) {
        if (data != null) {
            // Get the device MAC address
            ArrayList<String> chooseDeviceList = data.getStringArrayListExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            new AcquireDataThread(chooseDeviceList, subHandler, commandType).run();
            mOutStringBuffer.append("Start...\n");
            textContext.setText(mOutStringBuffer.toString());
        }
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
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
