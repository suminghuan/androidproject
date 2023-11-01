package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

        findViewById(R.id.btn_store).setOnClickListener(new View.OnClickListener() { //儲存
            @Override
            public void onClick(View view) {
                String ChangeToString =inputText.getText().toString();
                int UserInputDistanceInt = Integer.parseInt(ChangeToString);
                JSONObject data = new JSONObject();
                try {
                    data.put("UserDistanceInt", UserInputDistanceInt);
                }catch (JSONException e){
                    e.printStackTrace();
                }
                SQLiteDAOInterface dao = new SQLiteDAO(distance_setting.this);
                boolean result = dao.insertDistance(data);
                if(result){
                    Toast.makeText(distance_setting.this,"儲存成功",Toast.LENGTH_LONG).show();
                }
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }



    }
}