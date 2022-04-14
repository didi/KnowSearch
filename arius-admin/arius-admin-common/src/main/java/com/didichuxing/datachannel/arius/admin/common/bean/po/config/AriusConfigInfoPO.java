package com.didichuxing.datachannel.arius.admin.common.bean.po.config;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AriusConfigInfoPO extends BasePO {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 配置组
     */
    private String  valueGroup;

    /**
     * 配置项的名称
     */
    private String  valueName;

    /**
     * 配置项的值
     */
    private String  value;

    /**
     * 配置项维度  1 集群   2 模板
     */
    private Integer dimension;

    /**
     * 1 正常  2 禁用  -1 删除
     */
    private Integer status;

    /**
     * 备注
     */
    private String  memo;

}
