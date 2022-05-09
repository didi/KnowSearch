package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexTemplateInfoWithPhyTemplates extends IndexTemplateInfo {

    /**
     * 物理模板信息
     */
    private List<IndexTemplatePhyInfo> physicals;

    public boolean hasPhysicals() {
        return CollectionUtils.isNotEmpty(physicals);
    }

    public IndexTemplatePhyInfo getMasterPhyTemplate() {
        if (CollectionUtils.isNotEmpty(physicals)) {
            for (IndexTemplatePhyInfo physical : physicals) {
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
    public List<IndexTemplatePhyInfo> fetchMasterPhysicalTemplates() {
        List<IndexTemplatePhyInfo> masterTemplates = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(physicals)) {
            for (IndexTemplatePhyInfo physical : physicals) {
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
    public IndexTemplatePhyInfo fetchMasterSlave(String groupId) {
        List<IndexTemplatePhyInfo> physicalsTemp = fetchMasterSlaves(groupId);
        if (!physicalsTemp.isEmpty()) {
            return physicalsTemp.get(0);
        }
        return null;
    }

    /**
     * 获取逻辑模板下GroupId相同的所有的物理模板列表
     * @param groupId 组ID
     * @return
     */
    public List<IndexTemplatePhyInfo> fetchMasterSlaves(String groupId) {
        List<IndexTemplatePhyInfo> templatePhysicals = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(physicals)) {
            for (IndexTemplatePhyInfo physical : physicals) {
                if (TemplateDeployRoleEnum.SLAVE.getCode().equals(physical.getRole()) &&
                        physical.getGroupId().equals(groupId)) {
                    templatePhysicals.add(physical);
                }
            }
        }
        return templatePhysicals;
    }

    public IndexTemplatePhyInfo getSlavePhyTemplate() {
        if (CollectionUtils.isNotEmpty(physicals)) {
            for (IndexTemplatePhyInfo physical : physicals) {
                if (physical.getRole().equals(TemplateDeployRoleEnum.SLAVE.getCode())) {
                    return physical;
                }
            }
        }
        return null;
    }

    public IndexTemplatePhyInfo getAnyOne() {
        if (CollectionUtils.isNotEmpty(physicals)) {
            return physicals.get(0);
        }
        return null;
    }
}
