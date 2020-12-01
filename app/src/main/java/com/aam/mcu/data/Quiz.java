package com.aam.mcu.data;

public class Quiz {
    String title, time, link, id;

    public Quiz() {
    }

    public Quiz(String title, String time, String link, String id) {
        this.title = title;
        this.time = time;
        this.link = link;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
