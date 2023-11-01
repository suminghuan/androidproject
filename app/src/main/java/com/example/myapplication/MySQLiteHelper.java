package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MyDatabase.db";//資料庫
    private static final int DATABASE_VERSION = 1;
    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS mutable(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "phoneNumber TEXT, " +
                "homeName TEXT, " +
                "mqttAcc TEXT, "+
                "mqttPass TEXT,"+
                "UserInputDistanceInt TEXT," +
                "savedMarkerLatLng TEXT" +
                ");";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 在此方法中處理資料庫版本更新的操作
        String dropTableQuery = "DROP TABLE IF EXISTS mutable";
        db.execSQL(dropTableQuery);
        onCreate(db);
    }
}
