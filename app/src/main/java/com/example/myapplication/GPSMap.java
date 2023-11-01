package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.Manifest;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class GPSMap extends AppCompatActivity implements LocationListener {

    private MapView mapView;
    private LocationManager locationManager;
    static final int REQUEST_LOCATION_PERMISSION = 1001;
    private GoogleMap googleMap;
    private LatLng savedMarkerLatLng;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //左上角關閉
        if(item.getItemId()==android.R.id.home){
            Intent intent=new Intent(GPSMap.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsmap);

        JSONObject data = new JSONObject();
        SQLiteDAOInterface dao = new SQLiteDAO(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this::onMapReady);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.storeGPS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    data.put("UserDeviceLocation", savedMarkerLatLng);
                }catch (JSONException e){
                    e.printStackTrace();
                }
                boolean result = dao.insertDeviceLocation(data);
                if(result){
                    Toast.makeText(GPSMap.this,"裝置位置儲存成功",Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // 設定地圖的點擊監聽器
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.clear();
                // 在地圖上加入標記
                googleMap.addMarker(new MarkerOptions().position(latLng).title("自訂標記"));

                // 儲存座標
                savedMarkerLatLng = latLng;
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            double latitude = lastKnownLocation.getLatitude();
            double longitude = lastKnownLocation.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);

            // 清除地圖上的所有標記
            googleMap.clear();

            // 添加新的標記
            googleMap.addMarker(new MarkerOptions().position(latLng).title("我的位置"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
        /*double latitude=0,longitude=0;
        LatLng latLng = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(latLng).title("我的位置"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void onLocationChanged(@NonNull Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // 取得 MapView
        MapView mapView = findViewById(R.id.mapView);

        // 取得 GoogleMap
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // 清除地圖上的所有標記
                googleMap.clear();

                // 創建一個標記並添加到地圖上
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title("My Location");
                googleMap.addMarker(markerOptions);

                // 移動地圖視角到新的位置
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
            }
        });
    }
    public void relocateButtonClick(View view) {
        // 檢查定位權限
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 獲取最新的位置
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);

                // 清除地圖上的所有標記
                googleMap.clear();

                // 添加新的標記
                googleMap.addMarker(new MarkerOptions().position(latLng).title("我的位置"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        } else {
            // 請求定位權限
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }




}