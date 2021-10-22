package com.didichuxing.datachannel.arius.admin;

import java.io.Serializable;

public class Result<T> implements Serializable {

    private T                 data;

    private String            message;

    private String            tips;

    private Integer           code;

    public boolean success() {
        return code == 0;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

}
