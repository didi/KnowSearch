package com.didichuxing.datachannel.arius.admin.common.bean.po.espackage;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 程序包版本管理
 * @author didi
 * @since 2020-08-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESPackagePO extends BasePO {

    private static final long serialVersionUID = 1L;

    private Long              id;

    /**
     * 镜像地址或包地址
     */
    private String            url;

    /**
     * 版本标识
     */
    private String            esVersion;

    /**
     * 包创建人
     */
    private String            creator;

    /**
     * 是否为发布版本
     */
    private Boolean           release;

    /**
     * 类型(3 docker/4 host)
     */
    private Integer           manifest;
    /**
     *备注
     */
    private String            desc;

    /**
     * 标记删除
     */
    private Boolean           deleteFlag;
}
