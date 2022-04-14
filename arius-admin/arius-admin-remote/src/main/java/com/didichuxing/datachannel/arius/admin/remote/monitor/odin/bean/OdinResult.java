package com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OdinResult<T> {
    private Integer code;

    private String  msg;

    private T       data;
}
