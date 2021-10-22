package com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.Data;

/**
 * ES插件包管理
 * @author didi
 * @since 2020-08-24
 */
@Data
public class ESPluginPO extends BasePO {

    private static final long serialVersionUID = 1L;

    /**
     * ID主键自增
     */
    private Long              id;

    /**
     * 插件名
     */
    private String            name;

    /**
     * 物理集群Id
     */
    private String physicClusterId;

    /**
     * 插件版本
     */
    private String            version;

    /**
     * 插件存储地址
     */
    private String            url;

    /**
     * 插件文件md5
     */
    private String            md5;

    /**
     * 插件描述
     */
    private String            desc;

    /**
     * 插件创建人
     */
    private String            creator;

    /**
     * S3上的url
     */
    private String            s3url;

    /**
     * 是否为系统默认： 0 否  1 是
     */
    private Boolean           pDefault;

    /**
     * 标记删除
     */
    private Boolean           deleteFlag;

    /**
     * 标记插件是否已经安装：0 未安装  1 已安装
     */
    private Boolean           installFlag;
}
