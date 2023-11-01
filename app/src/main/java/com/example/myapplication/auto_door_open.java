package com.example.myapplication;

import static com.example.myapplication.GPSMap.REQUEST_LOCATION_PERMISSION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class auto_door_open extends AppCompatActivity {

    // 地球半徑，單位：公里
    private static final double EARTH_RADIUS = 6371.0;
    private Switch switchAutoDoor;
    private Handler handler = new Handler();
    private boolean isTaskRunning = false;
    private LocationManager locationManager;
    private String SQLiteDeviceLocation;
    private LatLng CurrentLatLng;
    private double UserDistanceSetting;
    private Integer NeedMessage;//0為關，1為開
    private String android_id ;
    String OpenAuto = "open/"+android_id;
    String CloseAuto = "close/"+android_id;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //左上角關閉
        if(item.getItemId()==android.R.id.home){
            Intent intent=new Intent(auto_door_open.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_door_open);
        SQLiteDeviceLocation=null;
        UserDistanceSetting=0;
        android_id = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);


        switchAutoDoor = findViewById(R.id.switch1_auto_door_open);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        switchAutoDoor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(isChild()){
                    startTask();
                }else {
                    stopTask();
                }
            }
        });

    }

    private void startTask() {
        // 檢查是否已經有一個任務在運行
        if (!isTaskRunning) {
            isTaskRunning = true;

            // 定義一個無限循環的任務
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    // 執行你的操作
                    // 例如：更新 UI 或執行某些任務
                    if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // 獲取最新的位置
                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastKnownLocation != null) {
                            double latitude = lastKnownLocation.getLatitude();
                            double longitude = lastKnownLocation.getLongitude();
                            CurrentLatLng = new LatLng(latitude, longitude);
                        }
                    } else {
                        // 請求定位權限
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                    }
                    if(SQLiteDeviceLocation==null){
                        SQLiteDeviceLocation = retrieveDataFromDatabase();//調出SQLite裝置位置(double)
                    } else if (UserDistanceSetting==0) {
                        UserDistanceSetting = retrieveDataFromDatabaseForDistanceSetting();
                    }
                    LatLng DeviceLocationLatLng = parseLatLngFromString(SQLiteDeviceLocation);
                    double DeviceLocationLatitude = DeviceLocationLatLng.latitude;
                    double DeviceLocationLongitude = DeviceLocationLatLng.longitude;
                    double CurrentLocationLatitude = CurrentLatLng.latitude;
                    double CurrentLocationLongitude = CurrentLatLng.longitude;
                    double StraightLineDistance = calculateDistance(DeviceLocationLatitude,DeviceLocationLongitude,CurrentLocationLatitude,CurrentLocationLongitude);
                    if(StraightLineDistance<UserDistanceSetting){
                        if(NeedMessage!=1){
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    MqttCONNTER.publish("ControlMessage",2,OpenAuto.getBytes());
                                }
                            }).start();
                            NeedMessage=1;
                        }
                    } else if (StraightLineDistance>=UserDistanceSetting) {
                        if(NeedMessage!=0){
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    MqttCONNTER.publish("ControlMessage",2,CloseAuto.getBytes());
                                }
                            }).start();
                            NeedMessage=0;
                        }
                    }


                    // 執行完畢後，檢查 Switch 狀態
                    if (switchAutoDoor.isChecked()) {
                        // 如果 Switch 仍然是選中狀態，則繼續執行任務
                        handler.postDelayed(this, 1000); // 這裡延遲 1 秒後再次執行
                    } else {
                        // 如果 Switch 不再是選中狀態，則停止任務
                        isTaskRunning = false;
                    }
                }
            };

            // 開始執行任務
            handler.post(task);
        }
    }

    private void stopTask() {
        // 停止循環任務
        isTaskRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) { //計算距離
        // 將經度和緯度轉換為弧度
        double radiansLat1 = Math.toRadians(lat1);
        double radiansLon1 = Math.toRadians(lon1);
        double radiansLat2 = Math.toRadians(lat2);
        double radiansLon2 = Math.toRadians(lon2);

        // 計算經度和緯度的差異
        double diffLat = radiansLat2 - radiansLat1;
        double diffLon = radiansLon2 - radiansLon1;

        // 使用Haversine公式計算兩點之間的距離
        double a = Math.sin(diffLat / 2) * Math.sin(diffLat / 2)
                + Math.cos(radiansLat1) * Math.cos(radiansLat2)
                * Math.sin(diffLon / 2) * Math.sin(diffLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 返回兩點之間的直線距離
        return EARTH_RADIUS * c;
    }
    public String retrieveDataFromDatabase() {
        String data = null;

        // 定義你的查詢語句
        String query = "SELECT * FROM mutable"; // 替換為你的表名

        // 使用SQLiteDatabase的rawQuery方法執行查詢
        SQLiteDatabase readDB = null;
        Cursor cursor = readDB.rawQuery(query, null);

        // 檢查是否成功檢索數據
        if (cursor != null) {
            // 將游標移到第一行
            cursor.moveToFirst();

            // 遍歷游標以檢索數據
            while (!cursor.isAfterLast()) {
                // 假設你有一個名為"columnName"的列
                @SuppressLint("Range") String valueStr = cursor.getString(cursor.getColumnIndex("savedMarkerLatLng")); // 替換為你的列名
                data = valueStr;

                // 移動到下一行
                cursor.moveToNext();
            }

            // 關閉游標
            cursor.close();
        }

        return data;
    }
    public double retrieveDataFromDatabaseForDistanceSetting() {
        double data = 0.0; // 默认值可以根据您的需求更改

        // 定義你的查詢語句
        String query = "SELECT * FROM mutable"; // 替換為你的表名

        // 使用SQLiteDatabase的rawQuery方法執行查詢
        SQLiteDatabase readDB = null;
        Cursor cursor = readDB.rawQuery(query, null);

        // 檢查是否成功檢索數據
        if (cursor != null) {
            // 將游標移到第一行
            cursor.moveToFirst();

            // 遍歷游標以檢索數據
            while (!cursor.isAfterLast()) {
                // 假設你有一個名為"columnName"的列
                @SuppressLint("Range") String valueStr = cursor.getString(cursor.getColumnIndex("UserInputDistanceInt")); // 替換為你的列名
                try {
                    // 尝试将字符串转换为double
                    data = Double.parseDouble(valueStr);
                } catch (NumberFormatException e) {
                    // 处理转换失败的情况
                    e.printStackTrace();
                    // 或者可以添加默认值或其他错误处理逻辑
                }

                // 移動到下一行
                cursor.moveToNext();
            }

            // 關閉游標
            cursor.close();
        }

        return data;
    }

    private LatLng parseLatLngFromString(String latLngString) {
        String[] latLngArray = latLngString.split(",");

        if (latLngArray.length == 2) {
            String latitudeStr = latLngArray[0];
            String longitudeStr = latLngArray[1];

            try {
                double latitude = Double.parseDouble(latitudeStr);
                double longitude = Double.parseDouble(longitudeStr);
                return new LatLng(latitude, longitude);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return null; // 返回 null 表示解析失败
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttMessageReceived(MqttMessageEvent event) {
        String message = event.getMessage();

        // 在這裡顯示訊息
        TextView door_stata=findViewById(R.id.door_state);  //??
        door_stata.setText(message);
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