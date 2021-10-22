package com.didichuxing.datachannel.arius.admin.remote.employee.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EpHrInfoResult<T> {
    /**
     * 接口返回状态信息
     */
    private EpHrMedaInfo meta;
    /**
     * 接口返回具体数据
     */
    private T            data;
}
