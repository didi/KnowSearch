package com.didichuxing.datachannel.arius.admin.common.constant.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataType {
    /**
     * 业务类型代码
     */
    private Integer code;
    /**
     * 业务类型描述
     */
    private String desc;
    /**
     * 业务类型标签
     */
    private String label;
}
