package dgsw.hs.kr.smartrobot.util;

import dgsw.hs.kr.smartrobot.ConnectActivity;
import dgsw.hs.kr.smartrobot.model.DeviceInfo;

public class CarController {
    private static CarController INSTANCE = new CarController();

    private static final String START = "";
    private static final String END = "#";

    private static final String FORWARD = "f ";
    private static final String BACKWARD = "b ";
    private static final String STOP = "s";

    private static BluetoothController.ConnectedThread connectedThread = null;

    private ConnectionListener connectionCallback;

    private CarController() { }

    public static CarController getInstance() {
        return INSTANCE;
    }

    public static void setConnectedThread(BluetoothController.ConnectedThread connectedThread) {
        CarController.connectedThread = connectedThread;
    }

    public DeviceInfo getDevice() {
        if (!checkConnection()) return null;
        return connectedThread.getMmDevice();
    }

    public void setConnectionCallback(ConnectionListener connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    // NOTE: KDY - 코드 Simplify 할시 널 처리가 안됨으로 이렇게 두기바람.
    public boolean checkConnection() {
        if (connectedThread == null || !connectedThread.getMmDevice().isConnected()) {
            return false;
        }
        return true;
    }

    public void closeConnection() {
        if (connectedThread == null) return;

        connectedThread.cancel();
        connectedThread = null;
    }


    public boolean writeForward(int angle) {
        return write(START + FORWARD + Math.abs(angle) + END);
    }

    public boolean writeBackward(int angle) {
        return write(START + BACKWARD + Math.abs(angle) + END);
    }

    public boolean writeStop() {
        return write(START + STOP + END);
    }

    private boolean write(String str) {
        if(!checkConnection()) return false;
        boolean isSuccess = connectedThread.write(str);

        if (!isSuccess)
            if (connectionCallback != null)
                connectionCallback.OnClosed();

        return isSuccess;
    }

    public interface ConnectionListener {
        void OnClosed();
    }
}
