package com.didichuxing.datachannel.arius.admin.common.bean.entity.espackage;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.Data;

/**
 * @author lyn
 * @date 2021-01-12
 */
@Data
public class ESPackage extends BaseEntity {

    private Long id;

    /**
     * 镜像地址或包地址
     */
    private String url;

    /**
     * 版本标识
     */
    private String esVersion;

    /**
     * 包创建人
     */
    private String creator;

    /**
     * 是否为发布版本
     */
    private Boolean release;

    /**
     * 类型(3 docker/4 host)
     */
    private Integer manifest;
    /**
     * 备注
     */
    private String desc;

    /**
     * 标记删除
     */
    private Boolean deleteFlag;
}
