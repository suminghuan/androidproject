package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.widget.ImageButton;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton btn = findViewById(R.id.button_auto_door_open);
        ImageButton btn1 = findViewById(R.id.button_set_information); //btn id 待改
        ImageButton btn2 = findViewById(R.id.button_distance_setting);
        ImageButton btn3 = findViewById(R.id.button_location_choose);
        ImageButton btn4 = findViewById(R.id.button_control_door);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            // 已經具有定位權限，開始定位
            startLocationUpdates();
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, auto_door_open.class);
                startActivity(intent);
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, communication.class);
                startActivity(intent);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, distance_setting.class);
                startActivity(intent);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, location_selection.class);
                startActivity(intent);
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String android_id = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
                String doorStata= "get/"+android_id;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean sub_doorStata = MqttCONNTER.getInstance().subscribe("door_state_1", 2);
                        if (sub_doorStata) {
                            MqttCONNTER.getInstance().publish("door_state", 2, doorStata.getBytes());
                        }
                        }
                }).start();
                Intent intent=new Intent(MainActivity.this, control.class);
                startActivity(intent);
            }
        });


    }

    private void startLocationUpdates() {
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 獲取到定位權限，開始定位
                startLocationUpdates();
            } else {
                // 沒有獲取到定位權限，提示用戶或採取其他操作
            }
        }
    }
    @Subscribe
    public void onEvent(MqttMessage message){
        String payload=new String(message.getPayload());
        MqttMessageEvent door_stata_event=new MqttMessageEvent("door_stata_1",payload);
        EventBus.getDefault().post(door_stata_event);

    }
    protected void onResume() {
        super.onResume();
    }
    protected void onPause() {
        super.onPause();
    }
    protected void onDestory(){
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}