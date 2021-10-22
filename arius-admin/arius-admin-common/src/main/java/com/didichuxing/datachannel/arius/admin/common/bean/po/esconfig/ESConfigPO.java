package com.didichuxing.datachannel.arius.admin.common.bean.po.esconfig;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.Data;

/**
 * ES配置包列表
 * @author didi
 * @since 2020-08-24
 */
@Data
public class ESConfigPO extends BasePO {
    /**
     * ID主键自增
     */
    private Long    id;

    /**
     * 集群id
     */
    private Long    clusterId;

    /**
    * 配置文件名称
    */
    private String  typeName;

    /**
     * 角色名称
     */
    private String  enginName;

    /**
    * 配置内容
    */
    private String  configData;

    /**
     * 配置描述
     */
    private String  desc;

    /**
     * 配置tag
     */
    private String  versionTag;

    /**
     * 配置版本
     */
    private Integer versionConfig;

    /**
     * 是否在使用
     */
    private Integer selected;

    /**
     * 标记删除
     */
    private Boolean deleteFlag;
}
