package com.aam.mcu.data;

public class Post {

    String postTime, postBody, postImageUrl, uid, postId;

    public Post() {
    }

    public Post(String postTime, String postBody, String postImageUrl, String uid, String postId) {
        this.postTime = postTime;
        this.postBody = postBody;
        this.postImageUrl = postImageUrl;
        this.uid = uid;
        this.postId = postId;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getPostBody() {
        return postBody;
    }

    public void setPostBody(String postBody) {
        this.postBody = postBody;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
