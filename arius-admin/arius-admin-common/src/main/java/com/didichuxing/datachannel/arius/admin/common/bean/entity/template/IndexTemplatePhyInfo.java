package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplatePhysicalStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexTemplatePhyInfo extends BaseEntity {

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
     * 获取并且解析config
     * @return
     */
    public IndexTemplatePhysicalConfig fetchConfig() {
        IndexTemplatePhysicalConfig physicalConfig = null;
        if (StringUtils.isNotBlank(config)) {
            try {
                physicalConfig = JSON.parseObject(config, IndexTemplatePhysicalConfig.class);
            } catch (Exception e) {
                return null;
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
