package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

public class MQTT extends AppCompatActivity {
    String ETt_StringMQTTacc, ETt2_StringPhoneNumber, ETt1_StringMQTTpass, ETt3_StringUserName, ETt4_StringHomeName;
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            Intent intent=new Intent(MQTT.this, communication.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static final String MQTT_URL = "210.240.202.123";
    //private boolean b=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt1);

        ImageButton btn  = findViewById(R.id.button1_save);
        EditText ETt_MQTTacc  = findViewById(R.id.editTextTextPerson_MQTTacc);
        EditText ETt_MQTTpass = findViewById(R.id.editTextTextPerson_MQTTpass);
        EditText ETt_PhoneNumber = findViewById(R.id.editTextTextPersonPhoneNumber);
        EditText ETt_UserName = findViewById((R.id.editTextTextPersonUserName));
        EditText ETt_homeName = findViewById(R.id.editTextTextPersonhomeName);
        TextView show = findViewById(R.id.show_state);

        show.setText("請設定");
        String android_id = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
        String smqttpubTopic = "CreateUser";
        String smqttSubTopic = "DeviceState";


        JSONObject data = new JSONObject();
        SQLiteDAOInterface dao = new SQLiteDAO(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("帳號設定");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ETt_StringMQTTacc = ETt_MQTTacc.getText().toString();
                ETt1_StringMQTTpass = ETt_MQTTpass.getText().toString();
                ETt2_StringPhoneNumber = ETt_PhoneNumber.getText().toString();
                ETt3_StringUserName = ETt_UserName.getText().toString();
                ETt4_StringHomeName = ETt_homeName.getText().toString();

                if(!ETt_StringMQTTacc.isEmpty()&&
                        !ETt1_StringMQTTpass.isEmpty()&&
                        !ETt2_StringPhoneNumber.isEmpty()&&
                        !ETt3_StringUserName.isEmpty()&&
                        !ETt4_StringHomeName.isEmpty()) {

                        ETt_MQTTacc.setError(null);
                        ETt_MQTTpass.setError(null);
                        ETt_PhoneNumber.setError(null);
                        ETt_UserName.setError(null);
                        ETt_homeName.setError(null);

                    Thread thread_connect = new Thread(new Runnable() {     //connect
                        @Override
                        public void run() {
                            boolean b = MqttCONNTER.getInstance().CreateConnect("tcp://" + MQTT_URL + ":1883", ETt_StringMQTTacc, ETt1_StringMQTTpass, ETt2_StringPhoneNumber);
                            if (b) {
                                boolean sub = MqttCONNTER.getInstance().subscribe(smqttSubTopic, 2);
                                if (sub) {
                                    String smqttPubText = ETt_PhoneNumber.getText().toString() + "/" + ETt_UserName.getText().toString() + "/" + android_id;
                                    MqttCONNTER.getInstance().publish(smqttpubTopic, 2, smqttPubText.getBytes());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                data.put("mqttAcc", ETt_MQTTacc.getText().toString());
                                                data.put("mqttPass", ETt_MQTTpass.getText().toString());
                                                data.put("phoneNumber", ETt_PhoneNumber.getText().toString());
                                                data.put("homeName", ETt_homeName.getText().toString());
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            show.setText("請設定");
                                            boolean result = dao.insert(data);
                                            if (result) {
                                                show.setText("資料儲存成功");
                                            } else {
                                                show.setText("資料儲存失敗");
                                            }

                                        }
                                    });
                                }
                            }
                        }
                    });
                    thread_connect.start();
                }else if(ETt_StringMQTTacc.isEmpty()) {
                    ETt_MQTTacc.setError("請輸入帳號");
                }else if(ETt1_StringMQTTpass.isEmpty()){
                    ETt_MQTTpass.setError("請輸入密碼");
                }else if(ETt2_StringPhoneNumber.isEmpty()){
                    ETt_PhoneNumber.setError("請輸入手機號碼");
                }else if(ETt3_StringUserName.isEmpty()){
                    ETt_UserName.setError("請輸入使用者名稱");
                }else if(ETt4_StringHomeName.isEmpty()){
                    ETt_homeName.setError("請輸入家的名稱");
                }

            }
        });

    }

    @Subscribe
    public void onEvent(MqttMessage message){
        TextView show_text = findViewById(R.id.show_state);
        show_text.setText(message.toString());
    }
    protected void onResume() {
        super.onResume();
    }
    protected void onPause() {
        super.onPause();
    }
    protected void onDestory(){
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}