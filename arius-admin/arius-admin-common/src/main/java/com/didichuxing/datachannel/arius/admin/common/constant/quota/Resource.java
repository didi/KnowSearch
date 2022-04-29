package com.didichuxing.datachannel.arius.admin.common.constant.quota;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * 资源量
 * @author d06679
 * @date 2019/4/25
 */
@Data
@AllArgsConstructor
@ToString
public class Resource {
    /**
     * 内存，单位为core
     */
    private Double cpu;

    /**
     * 内存，单位为G
     */
    private Double mem;

    /**
     * 内存，单位为G
     */
    private Double disk;
}
