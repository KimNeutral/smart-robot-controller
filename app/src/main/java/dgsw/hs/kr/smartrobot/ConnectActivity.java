package dgsw.hs.kr.smartrobot;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import dgsw.hs.kr.smartrobot.fragment.DeviceFragment;
import dgsw.hs.kr.smartrobot.model.DeviceInfo;
import dgsw.hs.kr.smartrobot.util.BluetoothController;
import dgsw.hs.kr.smartrobot.util.CarController;

public class ConnectActivity extends AppCompatActivity {
    @BindView(R.id.btnScan)
    Button btnScan;

    @BindView(R.id.frameLayout)
    FrameLayout frameLayout;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private BluetoothController bluetoothController;

    private DeviceFragment fragment;

    private List<DeviceInfo> scannedDevices;

    private Handler mHandler;

    AlertDialog progressDialog;

    private DeviceFragment.OnListFragmentInteractionListener mDeviceClickListener = item -> {
        bluetoothController.connectTo(item);
        showProgressDialog();
    };

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                scannedDevices.add(new DeviceInfo(device.getName(), device.getAddress(), false));
                fragment.addItem(new DeviceInfo(device.getName(), device.getAddress(), false));
                Log.d("device found: ", device.toString());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ButterKnife.bind(this);

        initFragment();
        initProgressDialog();
        initBluetoothController();

        bluetoothController.turnBluetoothOn(this);

        scannedDevices = new ArrayList<>();
    }

    private void showIndicator() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideIndicator() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void initFragment() {
        fragment = DeviceFragment.newInstance(mDeviceClickListener);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.frameLayout, fragment);
        ft.commit();
    }

    private void initProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.please_wait)
                .setView(View.inflate(this, R.layout.alert_progress, null))
                .setCancelable(false);

        progressDialog = builder.create();
    }

    @SuppressLint("HandlerLeak")
    private void initBluetoothController() {
        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == BluetoothController.CONNECTING_STATUS){

                    if(msg.arg1 == 1){
                        DeviceInfo deviceInfo = (DeviceInfo)msg.obj;

                        Toast.makeText(getApplicationContext(), R.string.connection_success, Toast.LENGTH_SHORT).show();
                        CarController.setConnectedThread(bluetoothController.getConnectedThread(deviceInfo.getAddress()));
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
                    }

                    hideProgressDialog();
                }
            }
        };

        bluetoothController = new BluetoothController(this, mHandler);
    }

    private void startScan() {
        fragment.clearItem();
        bluetoothController.startScan(bReciever);
    }

    private void stopScan() {
        bluetoothController.stopScan();
    }

    private void toggleScan() {
        if(bluetoothController.isScanning()) {
            btnScan.setText(R.string.start_scanning);
            hideIndicator();
            stopScan();
        } else {
            btnScan.setText(R.string.stop_scanning);
            showIndicator();
            startScan();
        }
    }

    public void onScanButtonClicked(View view) {
        toggleScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
        dismissProgressDialog();
    }

    private void showProgressDialog() {
        progressDialog.show();
    }

    private void hideProgressDialog() {
        progressDialog.hide();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
