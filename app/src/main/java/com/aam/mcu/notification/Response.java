package com.aam.mcu.notification;

public class Response {
    public int success;

    public Response(int success) {
        this.success = success;
    }

    public Response() {
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }
}
