package com.example.myapplication;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.EventObject;

public class MqttcallBackBus implements MqttCallback {


    private static String massageData;


    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        EventBus.getDefault().post(new MqttMessageEvent(topic,new String(message.getPayload(),"UTF-8")));
        massageData= message.toString();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
    public static String getMQTTmessage(){
        return massageData;
    }
}