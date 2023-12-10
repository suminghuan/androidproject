package com.example.myapplication;

import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private HistoryListAdapter historyListAdapter;
    protected RecyclerView recyclerView;
    List<HistoryList> moiveList = new ArrayList<>();
    TextView doorhistoryText;
    private LocationManager locationManager;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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
        ImageButton btn_ADO = findViewById(R.id.button_auto_door_open);
        ImageButton btn_SI = findViewById(R.id.button_set_information);
        ImageButton btn_DS = findViewById(R.id.button_distance_setting);
        ImageButton btn_LC = findViewById(R.id.button_location_choose);
        ImageButton btn_CD = findViewById(R.id.button_control_door);
        doorhistoryText = findViewById(R.id.doorHistorytext);
        recyclerView = findViewById(R.id.doorHistory);


        //歷史紀錄轉換
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        historyListAdapter = new HistoryListAdapter(HistoryList.itemCallback);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(historyListAdapter);
        initHistorylist(moiveList);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION);
        } else {

        }

        btn_ADO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, auto_door_open.class);
                startActivity(intent);
            }
        });

        btn_SI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, communication.class);
                startActivity(intent);
            }
        });

        btn_DS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, distance_setting.class);
                startActivity(intent);
            }
        });

        btn_LC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, location_selection.class);
                startActivity(intent);
            }
        });

        btn_CD.setOnClickListener(new View.OnClickListener() {
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


    @Override
    protected void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )){
            new AlertDialog.Builder(this)
                    .setMessage("請開啟GPS定位")
                    .setPositiveButton("前往開啟", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent callGPSSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .show();
        }else{
            startLocationUpdates();
        }
    }
//
    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    protected void onResume() {
        if(doorHistoryCheckHomeName() ){       // doorHistory
            String android_id = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
            String doorHistory="getHistory/"+android_id;
            boolean sub_doorStata = MqttCONNTER.getInstance().subscribe("door_history_1", 0);
            if (sub_doorStata) {
                MqttCONNTER.getInstance().publish("door_history", 0, doorHistory.getBytes());
            }
        }else {
            TextView textView = findViewById(R.id.doorHistorytext);
            textView.setText("找不到紀錄");
        }
        super.onResume();
    }

    protected void onPause() {
        moiveList.clear();
        initHistorylist(moiveList);
        historyListAdapter.notifyDataSetChanged();
        historyListAdapter.submitList(moiveList);
        super.onPause();
    }

    public boolean doorHistoryCheckHomeName() {
        String query = "SELECT * FROM mutable"; // 替換為你的表名
        boolean message = false;

        // 使用SQLiteDatabase的rawQuery方法執行查詢
        MySQLiteHelper sqLiteHelper = new MySQLiteHelper(this);
        SQLiteDatabase readDB = sqLiteHelper.getReadableDatabase();
        Cursor cursor = readDB.rawQuery(query, null);
        // 檢查是否成功檢索數據
        if (cursor != null) {
            // 將游標移到第一行
            cursor.moveToFirst();

            // 遍歷游標以檢索數據
            while (!cursor.isAfterLast()) {

                String DBhomeName = cursor.getString(2);
                if (DBhomeName != null) {
                    message = true;
                }
                // 移動到下一行
                cursor.moveToNext();
            }
            // 關閉游標
            cursor.close();
        }
        readDB.close();
        return message;
    }


    public void initHistorylist(List<HistoryList> moiveList){
        moiveList.add(new HistoryList("狀態", "使用者", "日期", "時間"));
        historyListAdapter.submitList(moiveList);
        doorhistoryText.setText("沒有紀錄");
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttMessageReceived(MqttMessageEvent event) {
        String message = event.getMessage();

        if(event.getTopic().equals("door_state_1")) {
            TextView appState = findViewById(R.id.main_app_state);
            appState.setText("狀態："+message);
        }
        if(event.getTopic().equals("door_history_1")) {
            //處理收到的歷史紀錄
            String[] msgRow = message.split("[\n]");
            moiveList = new ArrayList<>(historyListAdapter.getCurrentList());
            for (String index : msgRow) {
                String[] msgIndexCol = index.split("[, ]");
                // 最多5筆記錄
                moiveList.add(new HistoryList(msgIndexCol[0], msgIndexCol[1], msgIndexCol[2], msgIndexCol[3]));
                historyListAdapter.submitList(moiveList);
            }
            doorhistoryText.setText("歷史紀錄");
        }
    }

    //TODO:GPS檢查過了但沒有存
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
//    @Subscribe
//    public void onEvent(@NonNull MqttMessage message){
//        String payload=new String(message.getPayload());
//        MqttMessageEvent door_stata_event=new MqttMessageEvent("door_stata_1",payload);
//        EventBus.getDefault().post(door_stata_event);
//
//    }
//    protected void onResume() {
//        super.onResume();
//    }
//    protected void onPause() {
//        super.onPause();
//    }
//    public void addItem(View view){
//
//    }
//    public void updateItem(View view){
//
//    }
//
}
