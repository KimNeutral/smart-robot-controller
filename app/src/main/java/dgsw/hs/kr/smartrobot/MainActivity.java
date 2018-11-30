package dgsw.hs.kr.smartrobot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import dgsw.hs.kr.smartrobot.util.CarController;

public class MainActivity extends AppCompatActivity implements JoystickListener, CarController.ConnectionListener {
    @BindView(R.id.tvConnection)
    TextView tvConnection;

    @BindView(R.id.joystick)
    Joystick joystick;

    @BindView(R.id.btnAxcel)
    Button btnAxcel;

    @BindView(R.id.btnBack)
    Button btnBack;

    private boolean joystickActive;
    private boolean axcelActive;
    private boolean backActive;

    private CarController carController = CarController.getInstance();

    private boolean doubleBackToExitPressedOnce = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        joystick.setJoystickListener(this);

        carController.setConnectionCallback(this);

        btnAxcel.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                axcelActive = true;
                if (checkConnection() && !joystickActive) {
                    writeForward(90);
                }
            }
            else if(checkConnection() && motionEvent.getAction() == MotionEvent.ACTION_UP) {
                writeStop();
                axcelActive = false;
            }
            return false;
        });

        btnBack.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                backActive = true;
                if (checkConnection() && !joystickActive) {
                    writeBackward(90);
                }
            } else if(checkConnection() && motionEvent.getAction() == MotionEvent.ACTION_UP) {
                writeStop();
                backActive = false;
            }
            return false;
        });
    }

    public void onConnectButtonClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), ConnectActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setConnectText();
    }

    @Override
    public void onDown() {
        joystickActive = true;
    }

    @Override
    public void onDrag(float degrees, float offset) {
        if(!checkConnection()) return;
        Log.d("onDrag: ", degrees + ", " + offset);

        if (axcelActive) {
            writeForward((int) degrees);
            Log.d("log: ", "forward");
        } else if (backActive) {
            writeBackward((int) degrees);
            Log.d("log: ", "backward");
        }
    }

    @Override
    public void onUp() {
        writeStop();
        joystickActive = false;
    }

    private void setConnectText() {
        if (checkConnection()) {
            String text = getString(R.string.connected) + carController.getDevice().getTitle();

            tvConnection.setText(text);
            tvConnection.setTextColor(Color.GREEN);
        } else {
            tvConnection.setText(R.string.disconnected);
            tvConnection.setTextColor(Color.RED);
        }
    }

    private boolean checkConnection() {
        return carController.checkConnection();
    }

    private void closeConnection() {
        carController.closeConnection();
    }

    private boolean writeForward(int angle) {
        return carController.writeForward(angle);
    }

    private boolean writeBackward(int angle) {
        return carController.writeBackward(angle);
    }

    private boolean writeStop() {
        return carController.writeStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (checkConnection()) {
            closeConnection();
        }
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_twice_to_exit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    // NOTE: Implemented from CarController.ConnectionListener
    @Override
    public void OnClosed() {
        carController.closeConnection();
        setConnectText();
        Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show();
    }
}
