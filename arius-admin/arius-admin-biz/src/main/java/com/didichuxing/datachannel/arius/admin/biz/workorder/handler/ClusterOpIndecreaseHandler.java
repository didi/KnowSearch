package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_DOCKER;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_HOST;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.OpOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.biz.worktask.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.biz.worktask.content.ClusterBaseContent;
import com.didichuxing.datachannel.arius.admin.biz.worktask.content.ClusterIndecreaseDockerContent;
import com.didichuxing.datachannel.arius.admin.biz.worktask.content.ClusterIndecreaseHostContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostsParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host.HostsScaleActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.ClusterOpIndecreaseDockerOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.ClusterOpIndecreaseHostOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.Getter;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 集群op indecrease处理程序
 *
 * @author
 * @date 2022/05/09
 */
@Service("clusterOpIndecreaseHandler")
@Deprecated
public class ClusterOpIndecreaseHandler extends BaseWorkOrderHandler {
    protected static final ILog  LOGGER = LogFactory.getLog(ClusterOpIndecreaseHandler.class);

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    @Autowired
    private ClusterPhyService esClusterPhyService;

    @Autowired
    private ESClusterService esClusterService;

    @Autowired
    private EcmHandleService ecmHandleService;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private OpTaskManager opTaskManager;

    @Override
    protected Result validateConsoleParam(WorkOrder workOrder) {

        ClusterBaseContent baseContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterBaseContent.class);

        if (ES_DOCKER.getCode() == baseContent.getType()) {
            ClusterIndecreaseDockerContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                ClusterIndecreaseDockerContent.class);

            if (AriusObjUtils.isNull(content.getPhyClusterId())) {
                return Result.buildParamIllegal("物理集群id为空");
            }

            ClusterPhy clusterPhy = esClusterPhyService.getClusterById(content.getPhyClusterId().intValue());
            if (AriusObjUtils.isNull(clusterPhy)) {
                return Result.buildParamIllegal("物理集群不存在");
            }

            if (opTaskManager.existUnClosedTask(content.getPhyClusterId().intValue(),
                OpTaskTypeEnum.CLUSTER_EXPAND.getType())) {
                return Result.buildParamIllegal("该集群上存在未完成的任务");
            }

            return Result.buildSucc();
        } else if (ES_HOST.getCode() == baseContent.getType()) {
            initParam(workOrder);
            ClusterIndecreaseHostContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                ClusterIndecreaseHostContent.class);

            if (AriusObjUtils.isNull(content.getPhyClusterId())) {
                return Result.buildParamIllegal("物理集群id为空");
            }

            ClusterPhy clusterPhy = esClusterPhyService.getClusterById(content.getPhyClusterId().intValue());
            if (AriusObjUtils.isNull(clusterPhy)) {
                return Result.buildParamIllegal("物理集群不存在");
            }

            if (opTaskManager.existUnClosedTask(content.getPhyClusterId().intValue(),
                    OpTaskTypeEnum.CLUSTER_EXPAND.getType())) {
                return Result.buildParamIllegal("该集群上存在未完成的集群扩缩容任务");
            }

            // 对于datanode的缩容，如果该节点上存在数据分片,做出警告
            if (content.getOperationType() == OpTaskTypeEnum.CLUSTER_SHRINK.getType()) {
                Map<String, Integer> segmentsOfIpByCluster = esClusterService.synGetSegmentsOfIpByCluster(content.getPhyClusterName());

                for (ESClusterRoleHost esClusterRoleHost : content.getClusterRoleHosts()) {
                    if (esClusterRoleHost.getRole().equals(ESClusterNodeRoleEnum.DATA_NODE.getDesc())
                            && segmentsOfIpByCluster.containsKey(esClusterRoleHost.getHostname())
                            && !segmentsOfIpByCluster.get(esClusterRoleHost.getHostname()).equals(0)) {
                        return Result.buildFail("数据节点上存在分片，请迁移分片之后再进行该节点的缩容");
                    }
                }
            }

            return Result.buildSucc();
        } else {
            return Result.buildFail("type 类型不对");
        }

    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        ClusterBaseContent baseContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterBaseContent.class);
        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }

        return baseContent.getPhyClusterName() + workOrderTypeEnum.getMessage();
    }

    @Override
    protected Result validateConsoleAuth(WorkOrder workOrder) {
        if (!ariusUserInfoService.isOPByDomainAccount(workOrder.getSubmitor())) {
            return Result.buildOpForBidden("非运维人员不能操作集群扩缩容！");
        }

        return Result.buildSucc();
    }

    @Override
    protected Result<Void> validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    @Override
    protected Result<Void> doProcessAgree(WorkOrder workOrder, String approver) throws AdminOperateException {
        ClusterBaseContent clusterBaseContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterBaseContent.class);

        EcmTaskDTO esEcmTaskDTO = new EcmTaskDTO();
        esEcmTaskDTO.setWorkOrderId(workOrder.getId());
        esEcmTaskDTO.setTitle(workOrder.getTitle());
        esEcmTaskDTO.setCreator(workOrder.getSubmitor());
        esEcmTaskDTO.setType(clusterBaseContent.getType());

        if (ES_DOCKER.getCode() == clusterBaseContent.getType()) {
            ClusterIndecreaseDockerContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                ClusterIndecreaseDockerContent.class);
            esEcmTaskDTO.setPhysicClusterId(content.getPhyClusterId());
            esEcmTaskDTO.setOrderType(content.getOperationType());
            List<EcmParamBase> ecmParamBaseList = OpOrderTaskConverter.convert2EcmParamBaseList(ES_DOCKER,
                    OpTaskTypeEnum.valueOfType(content.getOperationType()), content);
            esEcmTaskDTO.setClusterNodeRole(ListUtils
                .strList2String(ecmParamBaseList.stream().map(EcmParamBase::getRoleName).collect(Collectors.toList())));
            esEcmTaskDTO.setEcmParamBaseList(ecmParamBaseList);
        } else if (ES_HOST.getCode() == clusterBaseContent.getType()) {
            ClusterIndecreaseHostContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                ClusterIndecreaseHostContent.class);
            esEcmTaskDTO.setPhysicClusterId(content.getPhyClusterId());
            esEcmTaskDTO.setOrderType(content.getOperationType());

            List<EcmParamBase> hostScaleParamBaseList = getHostScaleParamBaseList(content.getPhyClusterId().intValue(),
                content.getClusterRoleHosts(), content.getPidCount());

            esEcmTaskDTO.setClusterNodeRole(ListUtils.strList2String(
                hostScaleParamBaseList.stream().map(EcmParamBase::getRoleName).collect(Collectors.toList())));
            esEcmTaskDTO.setEcmParamBaseList(hostScaleParamBaseList);
        } else {
            return Result.buildFail("type 类型不对");
        }

        OpTaskDTO opTaskDTO = new OpTaskDTO();
        opTaskDTO.setExpandData(JSON.toJSONString(esEcmTaskDTO));
        opTaskDTO.setTaskType(esEcmTaskDTO.getOrderType());
        opTaskDTO.setCreator(workOrder.getSubmitor());
        Result<OpTask> result = opTaskManager.addTask(opTaskDTO);
        if (null == result || result.failed()) {
            return Result.buildFail("生成集群新建操作任务失败!");
        }

        return Result.buildSucc();
    }

    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        ClusterBaseContent baseContent = ConvertUtil.obj2ObjByJSON(JSON.parse(extensions),
            ClusterBaseContent.class);

        if (ES_DOCKER.getCode() == baseContent.getType()) {
            ClusterIndecreaseDockerContent content = JSON.parseObject(JSON.parse(extensions).toString(),
                ClusterIndecreaseDockerContent.class);

            return ConvertUtil.obj2Obj(content, ClusterOpIndecreaseDockerOrderDetail.class);
        } else if (ES_HOST.getCode() == baseContent.getType()) {
            ClusterIndecreaseHostContent content = JSON.parseObject(JSON.parse(extensions).toString(),
                ClusterIndecreaseHostContent.class);

            return ConvertUtil.obj2Obj(content, ClusterOpIndecreaseHostOrderDetail.class);
        }
        return null;

    }

    @Override
    public List<UserBriefVO> getApproverList(AbstractOrderDetail detail) {
        return getOPList();
    }

    @Override
    public Result checkAuthority(WorkOrderPO orderPO, String userName) {
        if (isOP(userName)) {
            return Result.buildSucc(true);
        }
        return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }

    private List<EcmParamBase> getHostScaleParamBaseList(Integer phyClusterId, List<ESClusterRoleHost> roleClusterHosts,
                                                         Integer pidCount) {
        List<String> roleNameList = new ArrayList<>();
        for (ESClusterRoleHost clusterRoleHost : roleClusterHosts) {
            if (!roleNameList.contains(clusterRoleHost.getRole())) {
                roleNameList.add(clusterRoleHost.getRole());
            }
        }

        List<EcmParamBase> ecmParamBaseList = ecmHandleService.buildEcmParamBaseList(phyClusterId, roleNameList)
            .getData();
        return buildHostScaleParamBaseList(roleClusterHosts, pidCount, roleNameList, ecmParamBaseList);
    }

    private void initParam(WorkOrder workOrder) {
        ClusterIndecreaseHostContent clusterOpIndecreaseHostContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterIndecreaseHostContent.class);
        if (ES_HOST.getCode() == clusterOpIndecreaseHostContent.getType()) {
            // 如果当前角色对应pid_count为null，则设置为默认值1
            if (null == clusterOpIndecreaseHostContent.getPidCount()) {
                clusterOpIndecreaseHostContent.setPidCount(ClusterConstant.DEFAULT_CLUSTER_PAID_COUNT);
            }

            // 填充工单中的ip字段,port端口号填充
            Map<String, String> portOfRoleMapFromHost = getPortOfRoleMapFromHost(clusterOpIndecreaseHostContent.getPhyClusterId());
            for (ESClusterRoleHost esClusterRoleHost : clusterOpIndecreaseHostContent.getClusterRoleHosts()) {
                esClusterRoleHost.setIp(Getter.strWithDefault(esClusterRoleHost.getIp(), esClusterRoleHost.getHostname()));
                esClusterRoleHost.setPort(portOfRoleMapFromHost.get(esClusterRoleHost.getRole()));
            }

            workOrder.setContentObj(JSON.toJSON(clusterOpIndecreaseHostContent));
        }
    }

    private List<EcmParamBase> buildHostScaleParamBaseList(List<ESClusterRoleHost> roleClusterHosts, Integer pidCount, List<String> roleNameList, List<EcmParamBase> ecmParamBaseList) {
        List<EcmParamBase> hostScaleParamBaseList = new ArrayList<>();
        for (String roleName : roleNameList) {
            List<String> hostnameList = new ArrayList<>();
            for (ESClusterRoleHost clusterRoleHost : roleClusterHosts) {
                if (roleName.equals(clusterRoleHost.getRole())) {
                    if (AriusObjUtils.isBlank(clusterRoleHost.getHostname())) {
                        continue;
                    }
                    hostnameList.add(clusterRoleHost.getHostname());
                }
            }
            for (EcmParamBase ecmParamBase : ecmParamBaseList) {
                if (roleName.equals(ecmParamBase.getRoleName())) {
                    HostsParamBase hostsParamBase = (HostsParamBase) ecmParamBase;

                    HostsScaleActionParam hostScaleActionParam = ConvertUtil.obj2Obj(hostsParamBase,
                        HostsScaleActionParam.class);
                    hostScaleActionParam.setPidCount(pidCount);
                    hostScaleActionParam.setHostList(hostnameList);
                    hostScaleActionParam.setNodeNumber(hostnameList.size());

                    hostScaleParamBaseList.add(hostScaleActionParam);
                }
            }
        }
        return hostScaleParamBaseList;
    }

    /**
     * 从db中获取物理集群角色下的端口号信息
     * @return 物理集群下的角色端口map
     */
    private Map<String/*角色名称*/, String/*端口号*/> getPortOfRoleMapFromHost(Long phyClusterId) {
        Map<String, String> rolePortMap = new HashMap<>(ESClusterNodeRoleEnum.values().length);
        for (ESClusterNodeRoleEnum param : ESClusterNodeRoleEnum.values()) {
            if (param != ESClusterNodeRoleEnum.UNKNOWN) {
                List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.getByRoleAndClusterId(phyClusterId, param.getDesc());
                // 默认采用8060端口进行es集群的搭建
                rolePortMap.put(param.getDesc(),
                        CollectionUtils.isEmpty(clusterRoleHosts) ? ClusterConstant.DEFAULT_PORT : clusterRoleHosts.get(0).getPort());
            }
        }

        return rolePortMap;
    }
}