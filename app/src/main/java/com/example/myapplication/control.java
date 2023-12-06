package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class control extends AppCompatActivity {
    Intent intent;
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //左上角關閉
        if(item.getItemId()==android.R.id.home){
            intent = new Intent(control.this,MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {        //左上角關閉
            actionBar.setTitle("開關門控制");
            actionBar.setSubtitle(MQTT.ETt4_StringHomeName);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ImageButton btn  = findViewById(R.id.button_Open);
        ImageButton btn1 = findViewById(R.id.button_Close);

        homeNameChk();
        String android_id = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
        String doorStata= "get/"+android_id;
        String OpenManual = "open/"+android_id;
        String CloseManual = "close/"+android_id;
        btn.setOnClickListener(new View.OnClickListener() {         //開門
            @Override
            public void onClick(View view) {
                homeNameChk();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MqttCONNTER.publish("ControlMessage",2,OpenManual.getBytes());//open/android_id
                    }
                }).start();
            }
        });
        btn1.setOnClickListener(new View.OnClickListener() {        //關門
            @Override
            public void onClick(View view) {
                homeNameChk();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MqttCONNTER.publish("ControlMessage",2,CloseManual.getBytes());//close/android_id
                    }
                }).start();
            }
        });
    }

    //@Subscribe
    //public void onEvent(MqttMessage message){
    //    TextView door_stata=findViewById(R.id.door_state);
    //    door_stata.setText(message.toString());
    //}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttMessageReceived(MqttMessageEvent event) {
        String message = event.getMessage();

        if(event.getTopic().equals("door_state_1")) {
            TextView door_state_log = findViewById(R.id.door_state);
            door_state_log.setText(message);
        }
    }
    public void homeNameChk(){
        if(MQTT.ETt4_StringHomeName==null){
            Toast.makeText(control.this, "請先設定通訊/地點選擇", Toast.LENGTH_LONG).show();
        }
    }
    protected void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消註冊訂閱者
        EventBus.getDefault().unregister(this);
    }

}