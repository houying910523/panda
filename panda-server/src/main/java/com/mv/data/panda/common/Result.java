package com.mv.data.panda.common;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * author: houying
 * date  : 17-6-29
 * desc  :
 */
public class Result {
    private int status;
    private Object data;
    private String message;

    public Result(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public Result(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Result(Object data) {
        this.status = 0;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("status", status);
        if (data != null) {
            map.put("data", data);
        }
        if (message != null) {
            map.put("message", message);
        }
        return map;
    }
}
