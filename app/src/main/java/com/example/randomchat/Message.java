package com.example.randomchat;

public class Message {

    private String message;
    private long time;
    private String from;
    private String type;

    public Message(){}

    public Message(String message, long time, String from, String type) {
        this.message = message;
        this.time = time;
        this.from = from;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
