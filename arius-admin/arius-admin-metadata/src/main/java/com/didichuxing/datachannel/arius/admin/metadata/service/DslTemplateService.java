package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.DslQueryLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslQueryLimit;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslTemplateESDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cjm
 */
@Service
public class DslTemplateService {
    @Value("${es.update.cluster.name}")
    private String metadataClusterName;
    @Autowired
    private DslTemplateESDAO dslTemplateESDAO;
    @Autowired
    private IndexTemplateService indexTemplateService;
    @Autowired
    private ClusterRegionService clusterRegionService;

    public Boolean updateDslTemplateQueryLimit(List<DslQueryLimitDTO> dslQueryLimitDTOList) {

        List<DslQueryLimit> dslQueryLimitList = new ArrayList<>();
        for (DslQueryLimitDTO dslQueryLimitDTO : dslQueryLimitDTOList) {
            // 排除无效的 dslTemplateMd5
            if (dslTemplateESDAO.getDslTemplateByKey(dslQueryLimitDTO.getProjectIdDslTemplateMd5()) != null) {
                dslQueryLimitList.add(ConvertUtil.obj2Obj(dslQueryLimitDTO, DslQueryLimit.class));
            }
        }
        return dslTemplateESDAO.updateQueryLimitByProjectIdDslTemplate(dslQueryLimitList);
    }

    /**
     * 获取查询模板信息
     * @param dslQueryLimitList
     * @return
     */
    public Map<String, DslTemplatePO> getDslTemplateByKeys(List<DslQueryLimitDTO> dslQueryLimitList) {
        List<DslQueryLimit> dslQueryLimits = ConvertUtil.list2List(dslQueryLimitList, DslQueryLimit.class);
        return dslTemplateESDAO.getDslTemplateByKeys(dslQueryLimits);
    }

    public Boolean updateDslTemplateStatus(Integer projectId,String dslTemplateMd5) {
        DslTemplatePO dslTemplatePO = dslTemplateESDAO.getDslTemplateByKey(projectId, dslTemplateMd5);
        if (dslTemplatePO == null) {
            // 如果没有该 dslTemplateMd5
            return false;
        }
        // getEnable() 如果为 null，表示当前是启用状态，反转模版启用状态
        if (dslTemplatePO.getEnable() == null) {
            dslTemplatePO.setEnable(false);
        } else {
            dslTemplatePO.setEnable(!dslTemplatePO.getEnable());
        }
        String ariusModifyTime =DateTimeUtil.getCurrentFormatDateTime();
        dslTemplatePO.setAriusModifyTime(ariusModifyTime);
        List<DslTemplatePO> dslTemplatePOList = new ArrayList<>();
        dslTemplatePOList.add(dslTemplatePO);
        return dslTemplateESDAO.updateTemplates(dslTemplatePOList);
    }

    public DslTemplatePO getDslTemplateDetail(Integer projectId, String dslTemplateMd5) {
        return dslTemplateESDAO.getDslTemplateByKey(projectId, dslTemplateMd5);
    }

    public Tuple<Long, List<DslTemplatePO>> getDslTemplatePage(Integer projectId, DslTemplateConditionDTO queryDTO)
            throws ESOperateException {
        return dslTemplateESDAO.getDslTemplatePage(projectId, queryDTO);
    }

    public Tuple<Long, List<DslTemplatePO>> getDslTemplatePageWithoutMetadataCluster(Integer projectId, DslTemplateConditionDTO queryDTO) throws ESOperateException {
        List<Long> logicClusterIds = getLogicClusterIds(metadataClusterName);
        List<String> templateNameList = indexTemplateService.listByResourceIds(logicClusterIds).stream()
                .map(IndexTemplate::getName).filter(indexName -> !indexName.startsWith("arius")).collect(Collectors.toList());
        return dslTemplateESDAO.getDslTemplatePageWithoutMetadataCluster(projectId, queryDTO, templateNameList);
    }

    /**
     * 获取逻辑集群ids
     *
     * @param phyClusterName
     * @return
     */
    private List<Long> getLogicClusterIds(String phyClusterName) {
        List<Long> logicClusterIdList = clusterRegionService.listPhyClusterRegions(phyClusterName)
                .stream().map(ClusterRegion::getLogicClusterIds)
                .filter(clusterLogicId -> !AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID.equals(clusterLogicId))
                .map(ListUtils::string2LongList).flatMap(Collection::stream).distinct().collect(Collectors.toList());
        return logicClusterIdList;
    }
}