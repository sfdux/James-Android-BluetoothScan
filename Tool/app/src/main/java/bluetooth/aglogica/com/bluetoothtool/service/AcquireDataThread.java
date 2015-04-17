package bluetooth.aglogica.com.bluetoothtool.service;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by James.Shi on 12/30/14.
 */
public class AcquireDataThread implements Runnable {

    private ExecutorService mThreadPool;
    private ArrayList<String> mChooseDeviceList;
    private BluetoothAdapter mBluetoothAdapter = null;
    private android.os.Handler mHandler;
    private boolean mSecure;
    private int mCommandType;

    public AcquireDataThread(ArrayList<String> chooseDeviceList, Handler handler, int commandType) {
        mThreadPool = Executors.newSingleThreadExecutor();
//        mThreadPool = Executors.newCachedThreadPool();
//        mThreadPool = Executors.newFixedThreadPool(7);
        mChooseDeviceList = chooseDeviceList;
        mHandler = handler;
        mCommandType = commandType;
    }

    @Override
    public void run() {
        ArrayList<AcquireDataSubThread> subThreadArrayList = new ArrayList<>();

        for (String address : mChooseDeviceList) {

            AcquireDataSubThread subThread = new AcquireDataSubThread(address, mHandler, mCommandType);

            subThreadArrayList.add(subThread);
        }

        if (!subThreadArrayList.isEmpty()) {
            try {
                List<Future<AbstractMap.SimpleEntry<Integer, Integer>>> futureList = mThreadPool.invokeAll(subThreadArrayList);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
