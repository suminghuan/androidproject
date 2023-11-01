package com.example.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.Manifest;

public class MyService extends Service implements LocationListener {

    private LocationManager locationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 初始化 LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 檢查使用者是否授予定位權限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 註冊位置更新的監聽器
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        // 返回 START_STICKY 表示如果 Service 被意外終止，系統會重新啟動 Service
        return START_STICKY;
    }

    public Object getSystemService(String locationService) {
        return null;
    }

    @Override
    public void onDestroy() {
        // 在 Service 被銷毀時移除位置更新的監聽器
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        // 在這裡處理位置更新的事件
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // 在這裡處理位置狀態變化的事件
    }

    @Override
    public void onProviderEnabled(String provider) {
        // 在這裡處理定位提供者啟用的事件
    }

    @Override
    public void onProviderDisabled(String provider) {
        // 在這裡處理定位提供者禁用的事件
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public double getLatitude(Location location){
        double latutude = location.getLatitude();
        return latutude;
    }
    private double getLongitude(Location location){
        double longitude = location.getLongitude();
        return longitude;
    }
}

