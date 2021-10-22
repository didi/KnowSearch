package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplatePhysicalStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
public class IndexTemplatePhy extends BaseEntity {

    /**
     * 主键
     */
    private Long id;

    /**
     * 逻辑模板id
     */
    private Integer logicId;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 表达式
     */
    private String expression;

    /**
     * 集群
     */
    private String cluster;

    /**
     * rack
     */
    private String rack;

    /**
     * shard个数
     */
    private Integer shard;

    /**
     * shardRouting个数
     */
    private Integer shardRouting;

    /**
     * 版本
     */
    private Integer version;

    /**
     * 角色
     * @see TemplateDeployRoleEnum
     */
    private Integer role;

    /**
     * 状态
     * @see TemplatePhysicalStatusEnum
     */
    private Integer status;

    /**
     * 配置 json格式
     */
    private String config;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 获取并且解析config
     * @return
     */
    public IndexTemplatePhysicalConfig fetchConfig() {
        IndexTemplatePhysicalConfig physicalConfig = null;
        if (StringUtils.isNotBlank(config)) {
            try {
                physicalConfig = JSON.parseObject(config, IndexTemplatePhysicalConfig.class);
            } catch (Exception e) {
            }
        }
        return physicalConfig;
    }

    /**
     * 获取默认写标识
     * @return
     */
    public Boolean fetchDefaultWriterFlags() {
        IndexTemplatePhysicalConfig physicalConfig = fetchConfig();
        if (physicalConfig != null && physicalConfig.getDefaultWriterFlags() != null) {
            return physicalConfig.getDefaultWriterFlags();
        }
        return AdminConstant.DEFAULT_WRITER_FLAGS;
    }

    /**
     * 获取组ID信息
     * @return
     */
    public String getGroupId() {
        IndexTemplatePhysicalConfig physicalConfig = fetchConfig();
        if (physicalConfig != null && StringUtils.isNotBlank(physicalConfig.getGroupId())) {
            return physicalConfig.getGroupId();
        }
        return AdminConstant.DEFAULT_GROUP_ID;
    }

}
