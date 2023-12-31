package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class distance_setting extends AppCompatActivity {       //左上角關閉

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            Intent intent = new Intent(distance_setting.this,MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_setting);
        EditText inputText = findViewById(R.id.input_distance_setting);
        ImageButton btn_store = findViewById(R.id.btn_store);

        btn_store.setOnClickListener(new View.OnClickListener() { //儲存
            @Override
            public void onClick(View view) {
                homeNameChk();
                String ChangeToString =inputText.getText().toString();
                if(!ChangeToString.equals("")) {
                    int UserInputDistanceInt = Integer.parseInt(ChangeToString);
                    JSONObject data = new JSONObject();
                    try {
                        data.put("UserDistanceInt", UserInputDistanceInt);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    SQLiteDAOInterface dao = new SQLiteDAO(distance_setting.this);
                    boolean result = dao.insertDistance(MQTT.ETt4_StringHomeName,data);     //以家名稱作為判斷基準
                    if (result) {
                        //儲存成功收鍵盤
                        try {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                        Toast.makeText(distance_setting.this, "儲存成功", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("開門距離設定");
            actionBar.setSubtitle(MQTT.ETt4_StringHomeName);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    public void homeNameChk(){
        if(MQTT.ETt4_StringHomeName==null){
            Toast.makeText(distance_setting.this, "請先設定通訊/地點選擇", Toast.LENGTH_LONG).show();
        }
    }
}