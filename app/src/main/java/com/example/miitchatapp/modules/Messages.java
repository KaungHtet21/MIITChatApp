package com.example.miitchatapp.modules;

import java.util.Objects;

public class Messages {
    String message, messageId, type, to, from, time, date, name;

    public Messages() {

    }

    public Messages(String message, String messageId, String type, String to, String from, String time, String date, String name) {
        this.message = message;
        this.messageId = messageId;
        this.type = type;
        this.to = to;
        this.from = from;
        this.time = time;
        this.date = date;
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Messages)
            return messageId.equals(((Messages) object).messageId);
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, messageId, type, to, from, time, date, name);
    }

    @Override
    public String toString() {
        return "Messages{" +
                "message='" + message + '\'' +
                ", messageId='" + messageId + '\'' +
                ", type='" + type + '\'' +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", time='" + time + '\'' +
                ", date='" + date + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
