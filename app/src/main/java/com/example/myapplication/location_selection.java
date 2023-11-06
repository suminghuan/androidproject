package com.example.myapplication;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import org.eclipse.paho.client.mqttv3.MqttException;
import java.util.List;

public class location_selection extends AppCompatActivity {
    private static final String MQTT_URL = "210.240.202.123";
    private static final String smqttSubTopic = "DeviceState";
    private static String SelectedItem;
    String ETt_StringMQTTacc, ETt1_StringMQTTpass, ETt2_StringPhoneNumber;
    private AutoCompleteTextView spinner;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_selection);
        // 初始化 Spinner
        spinner = findViewById(R.id.spinner);
        ImageButton btn = findViewById(R.id.btn_store);
        //顯示連線狀態
        TextView showState = findViewById(R.id.save_result);
        // 调用方法加载 homeName 数据
        loadHomeNames();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("地點選擇");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //斷線原本連接
                try {
                    MqttCONNTER.getInstance().disconnect();
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
                SelectedItem = spinner.getText().toString();
                if(!SelectedItem.equals("")){
                    //選取家在資料庫裡的全部資料
                    SpinnerSelectDB(SelectedItem);
                    //選取資料連接MQTT
                    boolean b = MqttCONNTER.getInstance().CreateConnect("tcp://" +
                                    MQTT_URL +
                                    ":1883",
                            ETt_StringMQTTacc,
                            ETt1_StringMQTTpass,
                            ETt2_StringPhoneNumber);
                    if (b) {
                        boolean sub = MqttCONNTER.getInstance().subscribe(smqttSubTopic, 2);
                        if (sub) {
                            showState.setText("連線成功");
                        }
                        else{
                            showState.setText("連線失敗");
                        }
                    }
                }
                }

        });
        spinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectedItem = spinner.getText().toString();
                if(!SelectedItem.equals("")) {
                    showState.setText("");
                }
            }
        });
    }

    //選取家在資料庫裡的全部資料
    private void SpinnerSelectDB(String SelectedItem){
        //選取那一個家
        MySQLiteHelper sqLiteHelper = new MySQLiteHelper(this);
        SQLiteDatabase readDB = sqLiteHelper.getReadableDatabase();
        //選取整個資料表
        String selectQuery = "SELECT * FROM mutable";
        Cursor cursor = readDB.rawQuery(selectQuery, null);
        //從搜尋資料庫裡選取的家
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            //找到選取的家將資料放入相對應MQTT連線參數
            String DBhomeName = cursor.getString(2);
            if(DBhomeName!= null){
                if( DBhomeName.equals(SelectedItem)){
                    ETt_StringMQTTacc = cursor.getString(3);
                    ETt1_StringMQTTpass = cursor.getString(4);
                    ETt2_StringPhoneNumber = cursor.getString(1);
                }
            }

            cursor.moveToNext();
        }
        cursor.close();
    }
    private void loadHomeNames() {

        SQLiteDAOInterface dao = new SQLiteDAO(this);
        // 从数据库中检索 homeName 数据
        List<String> homeNames = dao.getHomeNames(); // 从数据库中获取 homeName 列表
        // 创建 ArrayAdapter 并设置数据到 Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, homeNames);
        spinner.setAdapter(adapter);
    }
}