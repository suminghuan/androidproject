package com.example.myapplication;

import android.content.ContentValues;

import org.json.JSONObject;

import java.util.List;

public interface SQLiteDAOInterface {
    public void close();
    public boolean insert(JSONObject object);
    public boolean insertDistance(JSONObject object);
    public boolean insertDeviceLocation(JSONObject object);
    public int delete(String word );

    List<String> getHomeNames();
}
