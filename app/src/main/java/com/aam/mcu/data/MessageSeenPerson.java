package com.aam.mcu.data;

public class MessageSeenPerson {

    String uid, username, profileImageUrl;

    public MessageSeenPerson() {
    }

    public MessageSeenPerson(String uid, String username, String profileImageUrl) {
        this.uid = uid;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
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
}
