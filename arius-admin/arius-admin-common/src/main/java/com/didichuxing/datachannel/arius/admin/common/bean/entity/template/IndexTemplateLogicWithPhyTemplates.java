package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplateDeployRoleEnum;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
public class IndexTemplateLogicWithPhyTemplates extends IndexTemplateLogic {

    /**
     * 物理模板信息
     */
    private List<IndexTemplatePhy> physicals;

    public boolean hasPhysicals() {
        return CollectionUtils.isNotEmpty(physicals);
    }

    public IndexTemplatePhy getMasterPhyTemplate() {
        if (CollectionUtils.isNotEmpty(physicals)) {
            for (IndexTemplatePhy physical : physicals) {
                if (physical.getRole().equals(TemplateDeployRoleEnum.MASTER.getCode())) {
                    return physical;
                }
            }
        }
        return null;
    }

    /**
     * 获取逻辑索引模板所有的master物理所有模板列表
     * @return
     */
    public List<IndexTemplatePhy> fetchMasterPhysicalTemplates() {
        List<IndexTemplatePhy> masterTemplates = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(physicals)) {
            for (IndexTemplatePhy physical : physicals) {
                if (TemplateDeployRoleEnum.MASTER.getCode().equals(physical.getRole())) {
                    masterTemplates.add(physical);
                }
            }
        }
        return masterTemplates;
    }

    /**
     * 获取master对应的slave物理模板
     * @param groupId 组ID
     * @return
     */
    public IndexTemplatePhy fetchMasterSlave(String groupId) {
        List<IndexTemplatePhy> physicals = fetchMasterSlaves(groupId);
        if (!physicals.isEmpty()) {
            return physicals.get(0);
        }
        return null;
    }

    /**
     * 获取逻辑模板下GroupId相同的所有的物理模板列表
     * @param groupId 组ID
     * @return
     */
    public List<IndexTemplatePhy> fetchMasterSlaves(String groupId) {
        List<IndexTemplatePhy> templatePhysicals = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(physicals)) {
            for (IndexTemplatePhy physical : physicals) {
                if (TemplateDeployRoleEnum.SLAVE.getCode().equals(physical.getRole()) &&
                        physical.getGroupId().equals(groupId)) {
                    templatePhysicals.add(physical);
                }
            }
        }
        return templatePhysicals;
    }

    public IndexTemplatePhy getSlavePhyTemplate() {
        if (CollectionUtils.isNotEmpty(physicals)) {
            for (IndexTemplatePhy physical : physicals) {
                if (physical.getRole().equals(TemplateDeployRoleEnum.SLAVE.getCode())) {
                    return physical;
                }
            }
        }
        return null;
    }

    public IndexTemplatePhy getAnyOne() {
        if (CollectionUtils.isNotEmpty(physicals)) {
            return physicals.get(0);
        }
        return null;
    }
}
