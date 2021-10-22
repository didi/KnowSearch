package com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean;

import lombok.Data;

@Data
public class OdinResult<T> {
    private Integer code;

    private String  msg;

    private T       data;
}
