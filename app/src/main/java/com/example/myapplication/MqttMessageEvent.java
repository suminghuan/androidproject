package com.example.myapplication;

public class MqttMessageEvent {
    private String topic;
    private String message;
    public  MqttMessageEvent(String topic, String message) {
        this.topic = topic;
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessage() {
        return message;
    }

}
