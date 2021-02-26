package com.aam.mcu.data;

public class Chat {

    String message, uid, username, profileImageUrl, sendingTime, messageId;

    public Chat() {
    }

    public Chat(String message, String uid, String username, String profileImageUrl, String sendingTime, String messageId) {
        this.message = message;
        this.uid = uid;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.sendingTime = sendingTime;
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getSendingTime() {
        return sendingTime;
    }

    public void setSendingTime(String sendingTime) {
        this.sendingTime = sendingTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
