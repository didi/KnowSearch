package com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2020-12-29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESConfig extends BaseEntity {
    /**
     * ID主键自增
     */
    private Long    id;

    /**
     * 集群id
     */
    private Long    clusterId;

    /**
     * 配置文件名称 ：elasticsearch.yml
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
     *  原始配置内容(工单特有，其他实体P DTO无此字段)
     */
    private String  originalConfigData;

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
     *  是否在使用 0：不使用 1：部分生效中 2：已生效
     */
    private Integer selected;

    /**
     * 标记删除
     */
    private Boolean deleteFlag;
}
