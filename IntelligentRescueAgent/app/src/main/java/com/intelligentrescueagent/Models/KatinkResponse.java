package com.intelligentrescueagent.Models;

/**
 * Created by Angel Buzany on 24/07/2016.
 */
public class KatinkResponse {
    private int code;
    private String msg;
    private Object content;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
