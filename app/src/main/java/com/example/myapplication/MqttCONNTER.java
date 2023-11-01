package com.example.myapplication;

import android.widget.EditText;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttCONNTER {

    protected String UserMQ_Acc;
    protected String UserMQ_Password;
    protected String UserMQ_ID;
    private static MqttClient client;
    private static MqttConnectOptions MQ_OpT;
    private static MqttCallback mCallback;
    private MqttCONNTER(){mCallback=new MqttcallBackBus();}
    private  static MqttCONNTER mInstance = null;
    private MqttClientPersistence datastore;

    public boolean CreateConnect(String brokerUrl,String UserName,String Password,String ClientID){
        boolean flag = false;
        try {
            MQ_OpT=new MqttConnectOptions();
            MQ_OpT.setMqttVersion(4);
            MQ_OpT.setCleanSession(true);
            MQ_OpT.setKeepAliveInterval(60);
            if(Password!= null){
                MQ_OpT.setPassword(Password.toCharArray());
            }
            if(UserName!=null){
                MQ_OpT.setUserName(UserName);
            }
            client=new MqttClient(brokerUrl,ClientID,datastore);
            client.setCallback(mCallback);
            flag = DoConnect();
            //flag = client.isConnected(); //Debug



        } catch (MqttException e){
        }
        return flag;
    }
    /*public  boolean isCONNECTED(){
        boolean flag = false;
        try {
            flag = client.isConnected();
        }catch (Exception e){
            
        }
        return flag;
    }*/

    public boolean DoConnect(){
        boolean flag =false;
        if(client!=null){
            try {
                client.connect(MQ_OpT);
                flag=client.isConnected();
            }catch (Exception e){

            }
        }
        return flag;
    }
    public static MqttCONNTER getInstance(){
        if(null == mInstance){
            mInstance = new MqttCONNTER();
        }
        return mInstance;
    }


    public void disconnect() throws MqttException {
        if(client!=null&&client.isConnected()){client.disconnect();}
    }

    public static boolean publish(String topic, int qos, byte[] payload) {
        boolean flag = false;
        if(client!= null&&client.isConnected()){
            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);
            try{
                client.publish(topic,message);
                flag= true;
            }catch(MqttException e){}
        }
        return flag;
    }
    public static boolean subscribe(String topic, int qos) {
        boolean flag = false;
        if (client!=null&&client.isConnected()){

            try {
                client.subscribe(topic,qos);
                flag = true;
            } catch (MqttException e) {
            }
        }
        return flag;
    }

    public void onEvent(MqttMessage message){

    }
}
