package bluetooth.aglogica.com.bluetoothtool.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.UUID;
import java.util.concurrent.Callable;

import bluetooth.aglogica.com.bluetoothtool.MainActivity;
import bluetooth.aglogica.com.bluetoothtool.SystemConst;
import bluetooth.aglogica.com.bluetoothtool.SystemUtil;
import bluetooth.aglogica.com.bluetoothtool.Utility;

/**
 * Created by James.Shi on 12/30/14.
 */
public class AcquireDataSubThread implements Callable<AbstractMap.SimpleEntry<Integer, Integer>> {

    private static final UUID SOCKET_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String PIN_CODE = "0000";
    private static final String BLUETOOTH_METHOD_SET_PIN = "setPin";
    private static final String BLUETOOTH_METHOD_CREATE_BOND = "createBond";
    private static final int BLUETOOTH_CONNECT_TIME_OUT_IN_SECOND = 20;

    private static final int STATE_NONE = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECT_FAILED = 2;
    private static final int STATE_CONNECTED = 3;
    private static final int STATE_STREAM_INTERRUPTED = 4;
    private static final int STATE_STREAM_COMPLETED = 5;

    private BluetoothDevice mDevice;
    private final String mDeviceId;
    private boolean mKeepMainThreadAlive = true;
    private long lastActiveTime = 0;
    private long startConnectTime = 0;
    private int mState;
    private String mFileName;

    private ConnectThread connectThread;
    private StreamWatcherThread watcherThread;

    private final Handler mHandler;
    private int mCommandType;

    private String TAG = "AcquireDataSubThread";

    public AcquireDataSubThread(String address, Handler handler, int commandType) {
        mDeviceId = address;
        mHandler = handler;
        mCommandType = commandType;

        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mAdapter.isEnabled()) {
            mAdapter.enable();
        }

        mDevice = mAdapter.getRemoteDevice(address);
        setState(STATE_NONE);
    }

    @Override
    public AbstractMap.SimpleEntry<Integer, Integer> call() {
        try {

            String request = "";

            connect();

            while (STATE_CONNECTED != getState()) {
                try {
                    Thread.sleep(SystemConst.VALUE_ONE_SECOND);
                } catch (InterruptedException ex) {
                    writeToScreen(String.format("call()---%s\n", ex.getMessage()));
                    break;
                }

                if (System.currentTimeMillis() > startConnectTime + BLUETOOTH_CONNECT_TIME_OUT_IN_SECOND * SystemConst.VALUE_ONE_SECOND) {
                    connectThread.cancel();
                }

                if (STATE_CONNECT_FAILED == getState()) {
                    writeToScreen(String.format("%s is connected failed. Please check.\n", mDeviceId));
                    break;
                }
            }

            if (STATE_CONNECTED == getState()) {
                writeToScreen(String.format("%s is connected successfully.\n", mDeviceId));

                switch (mCommandType) {
                    case SystemConst.SEND_LED:
                        request = String.format("%s", SystemConst.LED_COMMAND);
                        break;
                    case SystemConst.SEND_CONFIG:
                        request = String.format("timestamp %s\r\n|%s", Utility.getCurrentDateTime(), SystemConst.COMMAND);
                        break;
                    case SystemConst.SEND_STREAM:
//                        request = String.format("%s", SystemConst.STREAM_COMMAND);
                        request = String.format("timestamp %s\r\n|%s", Utility.getCurrentDateTime(), SystemConst.STREAM_COMMAND);
                        break;
                    default:
                        break;
                }

                if (!SystemUtil.isStringNullOrEmpty(request)) {
                    sendRequest(request);
                } else {
                    writeToScreen("Send command is empty.");
                }

                writeToScreen(String.format("Completed...\n"));
                if (mCommandType != SystemConst.SEND_LED) {
                    sendRequest(SystemConst.RESET_COMMAND);
                }

            }

        } catch (Exception ex) {
            writeToScreen(String.format("call()---%s\n", ex.getMessage()));
        }

        return null;
    }

    private void writeToScreen(String message) {
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_SCREEN);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_MESSAGE, message);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public void stop() {
        reset();
    }

    public void connect() {
        reset();

        connectThread = new ConnectThread();
        new Thread(connectThread).start();
        setState(STATE_CONNECTING);
    }

    public void sendRequest(String request) {
        if (watcherThread != null) {
            watcherThread.sendRequest(request);
        }
    }

    private void reset() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (watcherThread != null) {
            watcherThread.cancel();
            watcherThread = null;
        }

        lastActiveTime = 0;
        setState(STATE_NONE);
    }

    private int getState() {
        return mState;
    }

    private void setState(int state) {
        mState = state;
    }

    private void startStreamWatcher(BluetoothSocket socket) {
        watcherThread = new StreamWatcherThread(socket);
        new Thread(watcherThread).start();
    }

    private void setPin(Class<?> bluetoothClass, BluetoothDevice device) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method autoPairMethod = bluetoothClass.getMethod(BLUETOOTH_METHOD_SET_PIN, new Class[]{byte[].class});
        autoPairMethod.invoke(device, new Object[]{PIN_CODE.getBytes()});
    }

    public void createBond(Class<?> bluetoothClass, BluetoothDevice device) throws Exception {
        Method createBondMethod = bluetoothClass.getMethod(BLUETOOTH_METHOD_CREATE_BOND);
        createBondMethod.invoke(device);
    }

    private class ConnectThread implements Runnable {

        private final BluetoothSocket mSocket;

        protected ConnectThread() {
            BluetoothSocket socket = null;

            try {
                //Not work stable in Android 4.3
//                setPin(mDevice.getClass(), mDevice);
//                createBond(mDevice.getClass(), mDevice);

//                socket = mDevice.createRfcommSocketToServiceRecord(SOCKET_UUID);
                socket = mDevice.createInsecureRfcommSocketToServiceRecord(SOCKET_UUID);
            } catch (Exception ex) {
            }

            mSocket = socket;
        }

        @Override
        public void run() {
            int tryCounts = 0;

            while (tryCounts < 3) {
                tryCounts++;

                try {
                    startConnectTime = System.currentTimeMillis();
                    mSocket.connect();
                    break;
                } catch (Exception ex) {
                    if (tryCounts == 3) {

                    }
                }
            }

            if (mSocket.isConnected()) {
                setState(STATE_CONNECTED);
                startStreamWatcher(mSocket);
            } else {
                setState(STATE_CONNECT_FAILED);
            }
        }

        protected void cancel() {
            try {
//                if (mSocket.isConnected()) {
                mSocket.close();
//                }
            } catch (IOException ex) {
            }
        }
    }

    private class StreamWatcherThread implements Runnable {

        private final BluetoothSocket mSocket;
        //        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        private final BufferedInputStream mInputStream;

        protected StreamWatcherThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                inputStream = mSocket.getInputStream();
                outputStream = mSocket.getOutputStream();
            } catch (IOException ex) {
            }

            mInputStream = new BufferedInputStream(inputStream, 128);
            mOutputStream = outputStream;
        }

        @Override
        public void run() {
            try {
                //Define a rough buffer to read the data.
                byte[] roughBuffer = new byte[1024];
//test
//                int bytesRead = 0;
//                String strFileContents = "";
//                while ((bytesRead = mInputStream.read(roughBuffer)) != -1) {
//                    strFileContents = new String(roughBuffer, 0, bytesRead);
//                    if(strFileContents.contains("Flash Initialized")){
//                        return;
//                    }
//                }

                while (true) {
                    byte[] exactBuffer;
                    int i = this.mInputStream.read(roughBuffer);

                    //If no data coming anymore, break the loop. It means no data coming in 5 seconds.
                    if (i == -1)
                        break;

                    //The length of the exact buffer is the same as the real length which rough buffer got.
                    exactBuffer = new byte[i];

                    //Copy the every byte from rough buffer to exact buffer.
                    for (int j = 0; j < i; j++) {
                        exactBuffer[j] = roughBuffer[j];
                    }

                    //Write binary data from exact buffer to .bin file.
//                    LogHelper.writeDeviceBinaryToOneFile(SystemUtil.getPetConsoleUnloadPath(mPetId, mDeviceId.replace(":", "_")), mDeviceId.replace(":", "_"), exactBuffer, SystemConst.EXTENSION_BINARY);
                    SystemUtil.writeDeviceBinaryToFile(SystemUtil.getBinFolderPath(), mDeviceId.replace(":", "_"), exactBuffer, SystemConst.EXTENSION_BINARY);
                    lastActiveTime = SystemClock.elapsedRealtime();
                }

            } catch (IOException ex) {
                Log.d(TAG, ex.getMessage());
            }
        }

        protected void sendRequest(String request) {
            try {
                this.mOutputStream.write(request.getBytes());

            } catch (IOException ex) {
                Log.d(TAG, ex.getMessage());
            }
        }

        protected void cancel() {
            try {
                if (mSocket.isConnected()) {
                    mSocket.close();
                }
            } catch (IOException closeSocketException) {
                Log.d(TAG, closeSocketException.getMessage());
            }
        }
    }
}
