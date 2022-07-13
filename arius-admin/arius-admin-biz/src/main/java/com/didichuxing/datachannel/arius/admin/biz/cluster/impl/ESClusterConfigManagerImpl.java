package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ESClusterConfigManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESConfigVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESClusterConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ESClusterConfigManagerImpl implements ESClusterConfigManager {
    @Autowired
    private ESClusterConfigService esClusterConfigService;
    @Autowired
    private OperateRecordService   operateRecordService;
    @Autowired
    private ClusterRoleService     clusterRoleService;

    /**
     * 编辑configdesc
     *
     * @param param     入参
     * @param operator  操作人或角色
     * @param projectId 项目id
     * @return {@code Result<Void>}
     */
    @Override
    public Result<Void> editConfigDesc(ESConfigDTO param, String operator, Integer projectId) {
        final Result<Void> checkProjectCorrectly = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (checkProjectCorrectly.failed()) {
            return checkProjectCorrectly;
        }
        final ESConfig oldEsConfig = esClusterConfigService.getEsClusterConfigById(param.getId());
        final Result<Void> result = esClusterConfigService.editConfigDesc(param);
        if (result.success()) {
            operateRecordService.save(new OperateRecord.Builder()
                .content(String.format("描述变更：【%s】->【%s】", oldEsConfig.getDesc(), param.getDesc()))
                .userOperation(operator).operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_CONF_FILE_CHANGE)
                .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER).bizId(Math.toIntExact(param.getId())).build());
        }
        return result;
    }

    /**
     * 获取ES集群模板config
     *
     * @param type 类型
     * @return {@code Result<ESConfigVO>}
     */
    @Override
    public Result<ESConfigVO> getEsClusterTemplateConfig(String type) {
        return Result
            .buildSucc(ConvertUtil.obj2Obj(esClusterConfigService.getEsClusterTemplateConfig(type), ESConfigVO.class));

    }

    /**
     * @param clusterId
     * @return
     */
    @Override
    public Result<Set<String>> gainEsClusterRoles(Long clusterId) {
        List<ClusterRoleInfo> clusterRoleInfos = clusterRoleService.getAllRoleClusterByClusterId(clusterId.intValue());
        return Result.buildSucc(clusterRoleInfos.stream().filter(Objects::nonNull).map(ClusterRoleInfo::getRole)
            .collect(Collectors.toSet()));
    }

    /**
     * @param configId
     * @return
     */
    @Override
    public Result<ESConfigVO> getEsClusterConfigById(Long configId) {
        return Result
            .buildSucc(ConvertUtil.obj2Obj(esClusterConfigService.getEsClusterConfigById(configId), ESConfigVO.class));
    }

    /**
     * 获得ES集群配置
     *
     * @param clusterId 集群id
     * @return {@code Result<List<ESConfigVO>>}
     */
    @Override
    public Result<List<ESConfigVO>> gainEsClusterConfigs(Long clusterId) {
        Result<List<ESConfig>> listResult = esClusterConfigService.listEsClusterConfigByClusterId(clusterId);
        return Result.buildSucc(ConvertUtil.list2List(listResult.getData(), ESConfigVO.class));
    }
}