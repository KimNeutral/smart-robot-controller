package dgsw.hs.kr.smartrobot.util;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import dgsw.hs.kr.smartrobot.model.DeviceInfo;

public class BluetoothController {
    public final static int REQUEST_BLUETOOTH = 1;
    public final static int MESSAGE_READ = 2;
    public final static int CONNECTING_STATUS = 3;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Map<String, ConnectedThread> connectedThreadMap = new HashMap<>();

    private BluetoothAdapter btAdapter;
    private BroadcastReceiver bReciever;

    private Context context;
    private Handler mHandler;

    private boolean isScanning = false;

    public BluetoothController(Context context, Handler mHandler) {
        this.btAdapter = getDefaultBluetoothAdapter();
        this.context = context;
        this.mHandler = mHandler;
    }

    private BluetoothAdapter getDefaultBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    private Context getContext() {
        return context;
    }

    public enum Code {
        REQUEST_BLUETOOTH(1), MESSAGE_READ(2), CONNECTING_STATUS(3);

        private int value;

        Code(int value) {
            this.value = value;
        }
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void startScan(BroadcastReceiver reciever) {
        if(isScanning) return;

        this.bReciever = reciever;

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(reciever, filter);
        btAdapter.startDiscovery();

        isScanning = true;
    }

    public void stopScan() {
        if(!isScanning) return;

        getContext().unregisterReceiver(bReciever);
        btAdapter.cancelDiscovery();

        isScanning = false;
    }

    public void turnBluetoothOn(Activity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH);

        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN }, 1);
        }

        if (!btAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }
    }

    public Set<BluetoothDevice> getPairedDevices() {
        return btAdapter.getBondedDevices();
    }

    public void connectTo(DeviceInfo deviceInfo) {
        final String address = deviceInfo.getAddress();
        final String name = deviceInfo.getName();

        // Spawn a new thread to avoid blocking the GUI one
        new Thread()
        {
            public void run() {
                boolean fail = false;

                BluetoothDevice device = btAdapter.getRemoteDevice(address);
                BluetoothSocket mBTSocket = null;

                try {
                    mBTSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    fail = true;
                    Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                }

                // Establish the Bluetooth socket connection.
                try {
                    mBTSocket.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        mBTSocket.close();
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                }

                if(!fail) {
                    deviceInfo.setConnected(true);

                    BluetoothController.ConnectedThread mConnectedThread = new BluetoothController.ConnectedThread(deviceInfo, mBTSocket, mHandler);
                    mConnectedThread.start();

                    connectedThreadMap.put(address, mConnectedThread);

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, deviceInfo)
                            .sendToTarget();
                }
            }
        }.start();
    }

    public ConnectedThread getConnectedThread(String address) {
        if (!connectedThreadMap.containsKey(address)) {
            return null;
        }
        return connectedThreadMap.get(address);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    public class ConnectedThread extends Thread {
        private final Handler mmHandler;
        private final DeviceInfo mmDevice;
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(DeviceInfo device, BluetoothSocket socket, Handler handler) {
            mmDevice = device;
            mmSocket = socket;
            mmHandler = handler;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            mmDevice.setConnected(true);

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (mmDevice.isConnected()) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mmHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public boolean write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
            mmDevice.setConnected(false);
        }

        public DeviceInfo getMmDevice() {
            return mmDevice;
        }
    }
}
