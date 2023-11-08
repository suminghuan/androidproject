package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SQLiteDAO implements SQLiteDAOInterface{

    private final SQLiteDatabase readDB,writeDB;

    public List<String> getHomeNames() {
        // 在这里编写从数据库中检索 homeName 数据的代码
        List<String> homeNames = new ArrayList<>();
        String selectQuery = "SELECT * FROM mutable";
        Cursor cursor = readDB.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            if(cursor.getString(2)!=null) {
                homeNames.add(cursor.getString(2));
                cursor.moveToNext();
            }else {
                cursor.moveToNext();
            }
        }
        cursor.close();
        close();
        // 通过查询数据库等方式获取 homeName 数据，并添加到 homeNames 列表中
        return homeNames;
    }
    public SQLiteDAO(Context context){
        super();
        MySQLiteHelper sqLiteHelper = new MySQLiteHelper(context);
        readDB = sqLiteHelper.getReadableDatabase();
        writeDB = sqLiteHelper.getWritableDatabase();
    }

    @Override
    public void close() {
        readDB.close();
        writeDB.close();
    }

    @Override
    public boolean insert(JSONObject object) {
        ContentValues values = new ContentValues();
        try {
            String mqttAcc = object.getString("mqttAcc");
            String mqttPass = object.getString("mqttPass");
            String phoneNumber = object.getString("phoneNumber");
            String homeName = object.getString("homeName");

            //資料插入
            values.put("mqttAcc", mqttAcc);
            values.put("mqttPass", mqttPass);
            values.put("phoneNumber", phoneNumber);
            values.put("homeName", homeName);
        }catch (JSONException e){
            e.printStackTrace();
            return false;
        }
        long result = writeDB.insert("mutable",null,values);
        return result != -1;
    }

    // DONE: 2023/11/3 讓位置的資料與設定之距離能夠加到資料庫當前使用者的對應欄位

    public boolean insertDistance(String homeName, JSONObject object){

        ContentValues values = new ContentValues();
        try {
            String UserInputDistanceInt = object.getString("UserDistanceInt");

            //資料插入
            values.put("UserInputDistanceInt",UserInputDistanceInt);
        }catch (JSONException e){
            e.printStackTrace();
            return false;
        }
        //UserInputDistanceInt新增 homeName 的那列下
        long result = writeDB.update("mutable",values, "homeName = ?", new String[]{String.valueOf(homeName)});
        return result !=-1;

    }

    public boolean insertDeviceLocation( String homeName, JSONObject object){
        ContentValues values = new ContentValues();
        try {
            String UserDeviceLocation = object.getString("UserDeviceLocation");
            values.put("savedMarkerLatLng",UserDeviceLocation);
        }catch (JSONException e){
            e.printStackTrace();
            return false;
        }
        //將savedMarkerLatLng新增到 homeName 的那列下
        long result = writeDB.update("mutable",values, "homeName = ?", new String[]{String.valueOf(homeName)});
        return result != -1;
    }

    @Override
    public int delete(String word) {
        String whereClause = "mqttAcc = ?";
        String[] whereArgs = {word};

        int rowsDeleted = writeDB.delete("mutable", whereClause, whereArgs);
        return rowsDeleted;
    }
}