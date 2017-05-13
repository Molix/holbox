package com.baires.holboxclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText("Sistema Holbox");

        ImageButton alarmButton = (ImageButton) findViewById(R.id.alarm_button);
        ImageButton commButton = (ImageButton) findViewById(R.id.comm_button);
        ImageButton camsButton = (ImageButton) findViewById(R.id.cams_button);
        alarmButton.setOnClickListener(this);
        commButton.setOnClickListener(this);
        camsButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alarm_button:
                Intent intent1 = new Intent(this, AlarmActivity.class);
                startActivity(intent1);
                break;
            case R.id.comm_button:
                Intent intent2 = new Intent(this, CommActivity.class);
                startActivity(intent2);
                break;
            case R.id.cams_button:
                Intent intent3 = new Intent(this, CamsActivity.class);
                startActivity(intent3);
                break;
            default:
                break;
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
