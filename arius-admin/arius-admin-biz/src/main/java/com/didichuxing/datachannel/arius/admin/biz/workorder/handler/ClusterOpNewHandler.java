package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.CREATE_MASTER_NODE_MIN_NUMBER;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.MASTER_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_DOCKER;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.ES_HOST;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.task.OpTaskManager;
import com.didichuxing.datachannel.arius.admin.biz.task.content.ClusterBaseContent;
import com.didichuxing.datachannel.arius.admin.biz.task.content.ClusterNewDockerContent;
import com.didichuxing.datachannel.arius.admin.biz.task.content.ClusterNewHostContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.utils.OpOrderTaskConverter;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleDocker;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.ESClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.ClusterOpNewDockerOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.ClusterOpNewHostOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didiglobal.knowframework.security.common.vo.user.UserBriefVO;
import com.google.common.collect.Maps;

/**
 * 集群op新处理程序
 *
 * @author
 * @date 2022/05/09
 */
@Service("clusterOpNewHandler")
@Deprecated
public class ClusterOpNewHandler extends BaseWorkOrderHandler {

    @Autowired
    private OpTaskManager       opTaskManager;

    @Autowired
    private ClusterPhyService   esClusterPhyService;

    @Autowired
    private ESPackageService    esPackageService;

    private static final String PARAM_ILLEGAL_TIPS = "集群缺少类型为%s的节点";

    @Override
    protected Result<Void> validateConsoleParam(WorkOrder workOrder) {
        Result<Void> initResult = initParam(workOrder);
        if (initResult.failed()) {
            return Result.buildFrom(initResult);
        }

        ClusterBaseContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterBaseContent.class);
        Result<Void> doValidateHostTypeResult = validateClusterMasterNodeNumber(content, workOrder);
        if (doValidateHostTypeResult.failed()) {
            return doValidateHostTypeResult;
        }

        if (AriusObjUtils.isNull(content.getPhyClusterName())) {
            return Result.buildParamIllegal("物理集群名称为空");
        }

        if (esClusterPhyService.isClusterExists(content.getPhyClusterName())) {
            return Result.buildParamIllegal("物理集群名称不能重复");
        }

        //ES同一个角色的端口号应该相同，拆解ip和port后进行校验
        Result<Void> doValidRoleClusterPort = validRoleClusterPort(workOrder);
        if (doValidRoleClusterPort.failed()) {
            return Result.buildFrom(doValidRoleClusterPort);
        }

        ClusterNewHostContent clusterOpNewHostContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterNewHostContent.class);
        // es版本的
        if (null == esPackageService.getByVersionAndType(clusterOpNewHostContent.getEsVersion(), ES_HOST.getCode())) {
            return Result.buildFail(ES_HOST.getDesc() + "类型版本为" + clusterOpNewHostContent.getEsVersion() + "的程序包不存在");
        }

        return Result.buildSucc();
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        ClusterBaseContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterBaseContent.class);
        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getPhyClusterName() + workOrderTypeEnum.getMessage();
    }

    @Override
    protected Result<Void> validateConsoleAuth(WorkOrder workOrder) {
        if (!isOP(workOrder.getSubmitor())) {
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

        EcmTaskDTO ecmTaskDTO = new EcmTaskDTO();
        ecmTaskDTO.setWorkOrderId(workOrder.getId());
        ecmTaskDTO.setTitle(workOrder.getTitle());
        ecmTaskDTO.setOrderType(OpTaskTypeEnum.CLUSTER_NEW.getType());
        ecmTaskDTO.setType(clusterBaseContent.getType());
        ecmTaskDTO.setCreator(workOrder.getSubmitor());
        ecmTaskDTO.setPhysicClusterId(ClusterConstant.INVALID_VALUE);

        // 工单数据 转 handle data
        List<EcmParamBase> ecmParamBaseList = null;
        if (ES_DOCKER.getCode() == clusterBaseContent.getType()) {
            ecmParamBaseList = OpOrderTaskConverter.convert2EcmParamBaseList(ESClusterTypeEnum.ES_DOCKER,
                OpTaskTypeEnum.CLUSTER_NEW,
                ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterNewDockerContent.class));
        } else if (ES_HOST.getCode() == clusterBaseContent.getType()) {
            // 获取并且设置新建集群工单内容中的集群创建人信息
            ClusterNewHostContent clusterOpNewHostContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
                ClusterNewHostContent.class);
            clusterOpNewHostContent.setCreator(workOrder.getSubmitor());

            ecmParamBaseList = OpOrderTaskConverter.convert2EcmParamBaseList(ESClusterTypeEnum.ES_HOST,
                OpTaskTypeEnum.CLUSTER_NEW, clusterOpNewHostContent);
        } else {
            return Result.buildFail("集群类型(Docker|Host)错误");
        }

        ecmTaskDTO.setEcmParamBaseList(ecmParamBaseList);

        OpTaskDTO opTaskDTO = new OpTaskDTO();
        opTaskDTO.setTaskType(OpTaskTypeEnum.CLUSTER_NEW.getType());
        opTaskDTO.setCreator(workOrder.getSubmitor());
        opTaskDTO.setExpandData(ConvertUtil.obj2Json(ecmTaskDTO));

        Result<OpTask> result = opTaskManager.addTask(opTaskDTO, AuthConstant.SUPER_PROJECT_ID);
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
        ClusterBaseContent clusterBaseContent = ConvertUtil.obj2ObjByJSON(JSON.parse(extensions),
            ClusterBaseContent.class);

        if (ES_DOCKER.getCode() == clusterBaseContent.getType()) {
            ClusterNewDockerContent content = JSON.parseObject(JSON.parse(extensions).toString(),
                ClusterNewDockerContent.class);

            return ConvertUtil.obj2Obj(content, ClusterOpNewDockerOrderDetail.class);
        } else if (ES_HOST.getCode() == clusterBaseContent.getType()) {
            ClusterNewHostContent content = JSON.parseObject(JSON.parse(extensions).toString(),
                ClusterNewHostContent.class);

            return ConvertUtil.obj2Obj(content, ClusterOpNewHostOrderDetail.class);
        } else {
            return null;
        }
    }

    @Override
    public List<UserBriefVO> getApproverList(AbstractOrderDetail detail) {
        return getOPList();
    }

    @Override
    public Result<Void> checkAuthority(WorkOrderPO orderPO, String userName) {
        if (isOP(userName)) {
            return Result.buildSucc();
        }
        return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }

    /*******************************************private************************************************/

    private Result<Void> validateClusterMasterNodeNumber(ClusterBaseContent content, WorkOrder workOrder) {
        if (ES_HOST.getCode() == content.getType()) {
            return validateClusterMasterNodeNumberESHost(workOrder);
        } else if (ES_DOCKER.getCode() == content.getType()) {
            return validateClusterMasterNodeNumberESDocker(workOrder);
        }
        return Result.buildSucc();
    }

    private Result<Void> initParam(WorkOrder workOrder) {
        ClusterNewHostContent clusterOpNewHostContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterNewHostContent.class);

        // 校验pid_count（单节点实例数字段）,如果为null，则设置默认值1
        if (null == clusterOpNewHostContent.getPidCount()) {
            clusterOpNewHostContent.setPidCount(ClusterConstant.DEFAULT_CLUSTER_PAID_COUNT);
        }

        // 对于address字段进行ip和端口号的拆分
        List<ESClusterRoleHost> roleClusterHosts = clusterOpNewHostContent.getClusterRoleHosts();

        for (ESClusterRoleHost esClusterRoleHost : roleClusterHosts) {
            if (null == esClusterRoleHost.getAddress()) {
                return Result.buildFail("传入节点的address不应该为空");
            }
            // 将ip和port中hostname中拆分出来
            String[] ipAndPort = esClusterRoleHost.getAddress().split(":");
            if (ipAndPort.length < 2) {
                return Result.buildFail("传入节点的address应该满足【ip:port】格式");
            }
            esClusterRoleHost.setHostname(ipAndPort[0]);
            esClusterRoleHost.setIp(ipAndPort[0]);
            esClusterRoleHost.setPort(ipAndPort[1]);
        }

        workOrder.setContentObj(JSON.toJSON(clusterOpNewHostContent));
        return Result.buildSucc();
    }

    private Result<Void> validateClusterMasterNodeNumberESDocker(WorkOrder workOrder) {
        ClusterBaseContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterNewDockerContent.class);

        List<ESClusterRoleDocker> roleClusterDockers = ((ClusterNewDockerContent) content).getRoleClusters();
        if (CollectionUtils.isEmpty(roleClusterDockers)) {
            return Result.buildParamIllegal("集群角色为空");
        }

        Set<String> dockerRoles = roleClusterDockers.stream().map(ESClusterRoleDocker::getRole)
            .collect(Collectors.toSet());
        if (!dockerRoles.contains(MASTER_NODE.getDesc())) {
            return Result.buildParamIllegal(String.format(PARAM_ILLEGAL_TIPS, MASTER_NODE.getDesc()));
        }

        Integer masterNodesNumber = roleClusterDockers.stream().filter(r -> MASTER_NODE.getDesc().equals(r.getRole()))
            .map(ESClusterRoleDocker::getPodNumber).collect(Collectors.toList()).get(0);

        if (masterNodesNumber < CREATE_MASTER_NODE_MIN_NUMBER) {
            return Result.buildParamIllegal("masternode角色pod数量要求大于等于1");
        }
        return Result.buildSucc();
    }

    private Result<Void> validateClusterMasterNodeNumberESHost(WorkOrder workOrder) {
        ClusterBaseContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(), ClusterNewHostContent.class);

        List<ESClusterRoleHost> roleClusterHosts = ((ClusterNewHostContent) content).getClusterRoleHosts();
        if (CollectionUtils.isEmpty(roleClusterHosts)) {
            return Result.buildParamIllegal("集群角色为空");
        }

        Set<String> hostRoles = roleClusterHosts.stream().map(ESClusterRoleHost::getRole).collect(Collectors.toSet());
        if (!hostRoles.contains(MASTER_NODE.getDesc())) {
            return Result.buildParamIllegal(String.format(PARAM_ILLEGAL_TIPS, MASTER_NODE.getDesc()));
        }

        List<ESClusterRoleHost> masterNodes = roleClusterHosts.stream()
            .filter(r -> MASTER_NODE.getDesc().equals(r.getRole())).collect(Collectors.toList());

        if (masterNodes.size() < CREATE_MASTER_NODE_MIN_NUMBER) {
            return Result.buildParamIllegal("masternode角色ip个数要求大于等于1");
        }
        return Result.buildSucc();
    }

    private Result<Void> validRoleClusterPort(WorkOrder workOrder) {
        ClusterNewHostContent clusterOpNewHostContent = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ClusterNewHostContent.class);
        List<ESClusterRoleHost> roleClusterHosts = clusterOpNewHostContent.getClusterRoleHosts();

        Map<Object, Object> roleClusterPortMap = Maps.newHashMap();
        for (ESClusterRoleHost esClusterRoleHost : roleClusterHosts) {
            // 如果map中不存在角色ip信息，则进行put
            if (!roleClusterPortMap.containsKey(esClusterRoleHost.getRole())) {
                roleClusterPortMap.put(esClusterRoleHost.getRole(), esClusterRoleHost.getPort());
                continue;
            }

            if (roleClusterPortMap.containsKey(esClusterRoleHost.getRole())
                && !roleClusterPortMap.get(esClusterRoleHost.getRole()).equals(esClusterRoleHost.getPort())) {
                return Result.buildFail("同一个集群中同一角色的端口号应该相同");
            }
        }

        return Result.buildSucc();
    }
}