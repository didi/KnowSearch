package com.didichuxing.datachannel.arius.admin.remote.employee.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EpHrMedaInfo {
    /**
     * 返回码，非0为失败
     */
    private Integer code;
    /**
     * 返回信息
     */
    private String  message;
}
