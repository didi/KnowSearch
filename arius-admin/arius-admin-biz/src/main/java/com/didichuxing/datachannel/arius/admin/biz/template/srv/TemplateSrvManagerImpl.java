package com.didichuxing.datachannel.arius.admin.biz.template.srv;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.RegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.CLUSTER;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.DELETE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EDIT;

@Service("templateSrvService")
@DependsOn("springTool")
public class TemplateSrvManagerImpl implements TemplateSrvManager {
    protected static final ILog   LOGGER                      = LogFactory.getLog(TemplateSrvManagerImpl.class);

    @Autowired
    private ClusterPhyService     clusterPhyService;

    @Autowired
    private ClusterLogicService   clusterLogicService;

    @Autowired
    private RegionRackService     esClusterRackService;

    @Autowired
    private OperateRecordService  operateRecordService;

    @Autowired
    private AriusUserInfoService  ariusUserInfoService;

    @Autowired
    private ClusterContextManager clusterContextManager;

    private static final String   COMMA                       = ",";

    private static final String   PHYSICAL_CLUSTER_NOT_EXISTS = "物理集群不存在";

    private static final String   NO_PERMISSION_CONTENT       = "只有运维或者研发才有权限操作";

    private static final String   CLUSTER_LOGIC_NOT_EXISTS    = "逻辑集群不存在";

    @Override
    public ClusterTemplateSrv getTemplateServiceBySrvId(int srvId) {
        return convertFromEnum(TemplateServiceEnum.getById(srvId));
    }

    @Override
    public List<String> getPhyClusterByOpenTemplateSrv(int srvId){
        List<String>     clusterPhyNames = new ArrayList<>();
        List<ClusterPhy> clusterPhies = clusterPhyService.listAllClusters();

        if(CollectionUtils.isEmpty(clusterPhies)){return clusterPhyNames;}

        clusterPhies.stream().forEach( clusterPhy -> {
            if(isPhyClusterOpenTemplateSrv(clusterPhy.getCluster(), srvId)){
                clusterPhyNames.add(clusterPhy.getCluster());
            }
        } );

        return clusterPhyNames;
    }

    @Override
    public boolean isPhyClusterOpenTemplateSrv(String phyCluster, int srvId) {
        try {
            Result<List<ClusterTemplateSrv>> result = getPhyClusterTemplateSrv(phyCluster);
            if (null == result || result.failed()) {
                return false;
            }

            List<ClusterTemplateSrv> clusterTemplateSrvs = result.getData();
            for (ClusterTemplateSrv templateSrv : clusterTemplateSrvs) {
                if (srvId == templateSrv.getServiceId()) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            LOGGER.warn("class=TemplateSrvManager||method=isPhyClusterOpenTemplateSrv||phyCluster={}||srvId={}",
                phyCluster, srvId, e);

            return true;
        }
    }

    @Override
    public Result<List<ClusterTemplateSrv>> getPhyClusterSelectableTemplateSrv(String phyCluster) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyCluster);
        if (null == clusterPhy) {
            return Result.buildNotExist(PHYSICAL_CLUSTER_NOT_EXISTS);
        }

        List<ClusterTemplateSrv> templateServices = new ArrayList<>();
        String clusterVersion = clusterPhy.getEsVersion();

        for (TemplateServiceEnum templateServiceEnum : TemplateServiceEnum.allTemplateSrv()) {
            String templateSrvVersion = templateServiceEnum.getEsClusterVersion().getVersion();

            if (!templateServiceEnum.isDefaultSrv()) {
                continue;
            }

            if (ESVersionUtil.isHigher(clusterVersion, templateSrvVersion)) {
                templateServices.add(convertFromEnum(templateServiceEnum));
            }
        }

        return Result.buildSucc(templateServices);
    }

    @Override
    public Result<List<ESClusterTemplateSrvVO>> getClusterLogicSelectableTemplateSrv(Long clusterLogicId) {
        if (Boolean.FALSE.equals(clusterLogicService.isClusterLogicExists(clusterLogicId))) {
            return Result.buildFail(CLUSTER_LOGIC_NOT_EXISTS);
        }

        ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(clusterLogicId);
        if (null == clusterLogicContext) {
            LOGGER.error(
                "class=TemplateSrvManagerImpl||method=getClusterLogicSelectableTemplateSrv||clusterLogicId={}||errMsg=failed to getClusterLogicContextFromCache",
                clusterLogicId);
            return Result.buildFail();
        }

        List<String> associatedClusterPhyNames = clusterLogicContext.getAssociatedClusterPhyNames();
        if (CollectionUtils.isNotEmpty(associatedClusterPhyNames)) {
            Result<List<ClusterTemplateSrv>> ret = getPhyClusterSelectableTemplateSrv(
                    associatedClusterPhyNames.get(0));
            if (ret.failed()) {
                return Result.buildFrom(ret);
            }

            return Result.buildSucc(ConvertUtil.list2List(ret.getData(), ESClusterTemplateSrvVO.class));
        }

        return Result.buildSucc();
    }

    @Override
    public Result<List<ClusterTemplateSrv>> getPhyClusterTemplateSrv(String phyCluster) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyCluster);
        if (null == clusterPhy) {
            return Result.buildNotExist(PHYSICAL_CLUSTER_NOT_EXISTS);
        }

        String templateSrvs = clusterPhy.getTemplateSrvs();
        if (StringUtils.isBlank(templateSrvs)) {
            return Result.buildSucc(new ArrayList<>(), "该物理集群无索引服务");
        }

        List<ClusterTemplateSrv> templateServices = new ArrayList<>();
        for (String strId : StringUtils.split(templateSrvs, COMMA)) {
            ClusterTemplateSrv templateSrv = getTemplateServiceBySrvId(Integer.valueOf(strId));
            if (null != templateSrv) {
                templateServices.add(templateSrv);
            }
        }

        return Result.buildSucc(templateServices);
    }

    @Override
    public Result<List<ESClusterTemplateSrvVO>> getClusterLogicTemplateSrv(Long clusterLogicId) {
        if (Boolean.FALSE.equals(clusterLogicService.isClusterLogicExists(clusterLogicId))) {
            return Result.buildFail(CLUSTER_LOGIC_NOT_EXISTS);
        }

        ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(clusterLogicId);
        if (null == clusterLogicContext) {
            LOGGER.error(
                "class=TemplateSrvManagerImpl||method=getClusterLogicTemplateSrv||clusterLogicId={}||errMsg=failed to getClusterLogicContextFromCache",
                clusterLogicId);
            return Result.buildFail();
        }

        List<String> associatedClusterPhyNames = clusterLogicContext.getAssociatedClusterPhyNames();
        if (CollectionUtils.isNotEmpty(associatedClusterPhyNames)) {
            Result<List<ClusterTemplateSrv>> ret = getPhyClusterTemplateSrv(associatedClusterPhyNames.get(0));
            if (ret.success()) {
                return Result.buildSucc(ConvertUtil.list2List(ret.getData(), ESClusterTemplateSrvVO.class));
            }
        }

        return Result.buildSucc();
    }

    @Override
    public List<Integer> getPhyClusterTemplateSrvIds(String phyCluster) {
        Result<List<ClusterTemplateSrv>> ret = getPhyClusterTemplateSrv(phyCluster);
        if (ret.success()) {
            return ret.getData().stream().map(ClusterTemplateSrv::getServiceId).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public Result<List<ClusterTemplateSrv>> getLogicClusterTemplateSrv(Long logicClusterId) {
        List<String> phyClusterNames = esClusterRackService.listPhysicClusterNames(logicClusterId);
        if (CollectionUtils.isEmpty(phyClusterNames)) {
            return Result.buildNotExist("逻辑集群对应的物理集群不存在");
        }

        Set<ClusterTemplateSrv> templateServiceTotals = new HashSet<>();

        for (String phyClusterName : phyClusterNames) {
            Result<List<ClusterTemplateSrv>> templateSrvsRet = getPhyClusterTemplateSrv(phyClusterName);
            if (null != templateSrvsRet && templateSrvsRet.success()) {
                for (ClusterTemplateSrv templateSrv : templateSrvsRet.getData()) {
                    templateServiceTotals.add(templateSrv);
                }
            }
        }

        return Result.buildSucc(new ArrayList<>(templateServiceTotals));
    }

    @Override
    public Result<Boolean> addTemplateSrv(String phyCluster, String templateSrvId, String operator) {
        ClusterTemplateSrv clusterTemplateSrv = getTemplateServiceBySrvId(Integer.valueOf(templateSrvId));
        if (null == clusterTemplateSrv) {
            return Result.buildNotExist("对应的索引服务不存在");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyCluster);
        if (null == clusterPhy) {
            return Result.buildNotExist(PHYSICAL_CLUSTER_NOT_EXISTS);
        }

        if (StringUtils.isBlank(clusterPhy.getTemplateSrvs())) {
            clusterPhy.setTemplateSrvs(templateSrvId);
        } else {
            List<String> templateSrvs = ListUtils.string2StrList(clusterPhy.getTemplateSrvs());
            if (!templateSrvs.contains(templateSrvId)) {
                clusterPhy.setTemplateSrvs(clusterPhy.getTemplateSrvs() + "," + templateSrvId);
            }else {
                return Result.buildSucc();
            }
        }

        Result<Boolean> result = clusterPhyService.editCluster(ConvertUtil.obj2Obj(clusterPhy, ESClusterDTO.class),
            operator);
        if (null != result && result.success()) {
            operateRecordService.save(CLUSTER, EDIT, phyCluster,
                phyCluster + "集群，增加一个索引服务：" + clusterTemplateSrv.getServiceName(), operator);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> addTemplateSrvForClusterLogic(Long clusterLogicId, String templateSrvId, String operator) {
        if (!isRDOrOP(operator)) {
            return Result.buildNotExist(NO_PERMISSION_CONTENT);
        }

        if (Boolean.FALSE.equals(clusterLogicService.isClusterLogicExists(clusterLogicId))) {
            return Result.buildFail(CLUSTER_LOGIC_NOT_EXISTS);
        }

        ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(clusterLogicId);
        if (null == clusterLogicContext) {
            LOGGER.error(
                "class=TemplateSrvManagerImpl||method=addTemplateSrvForClusterLogic||clusterLogicId={}||errMsg=failed to getClusterLogicContextFromCache",
                clusterLogicId);
            return Result.buildFail();
        }

        List<String> associatedClusterPhyNames = clusterLogicContext.getAssociatedClusterPhyNames();
        if (CollectionUtils.isEmpty(associatedClusterPhyNames)) {
            return Result.buildSucc();
        }

        for (String associatedClusterPhyName : associatedClusterPhyNames) {
            try {
                Result<Boolean> ret = addTemplateSrv(associatedClusterPhyName, templateSrvId, operator);
                if (ret.failed()) {
                    throw new ESOperateException("逻辑集群添加索引服务失败");
                }
            } catch (ESOperateException e) {
                LOGGER.error(
                    "class=TemplateSrvManagerImpl||method=addTemplateSrvForClusterLogic||clusterLogicId={}||errMsg={}",
                    clusterLogicId, e.getMessage());
            }
        }

        return Result.buildSucc();
    }

    @Override
    public Result<Boolean> replaceTemplateServes(String phyCluster, List<Integer> templateSrvIds, String operator) {
        if (!isRDOrOP(operator)) {
            return Result.buildNotExist(NO_PERMISSION_CONTENT);
        }
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyCluster);
        if (null == clusterPhy) {
            return Result.buildNotExist(PHYSICAL_CLUSTER_NOT_EXISTS);
        }

        clusterPhy.setTemplateSrvs(ListUtils.intList2String(templateSrvIds));
        return clusterPhyService.editCluster(ConvertUtil.obj2Obj(clusterPhy, ESClusterDTO.class), operator);

    }

    @Override
    public Result<Boolean> delTemplateSrv(String phyCluster, String templateSrvId, String operator) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyCluster);
        if (null == clusterPhy) {
            return Result.buildNotExist(PHYSICAL_CLUSTER_NOT_EXISTS);
        }

        List<String> templateSrvIds = ListUtils.string2StrList(clusterPhy.getTemplateSrvs());

        if (CollectionUtils.isEmpty(templateSrvIds)) {
            return Result.buildNotExist("物理集群的索引服务为空");
        }

        if (!templateSrvIds.contains(templateSrvId)) {
            return Result.buildNotExist("物理集群现有的索引服务不包含即将删除的索引服务");
        }

        templateSrvIds.remove(templateSrvId);
        clusterPhy.setTemplateSrvs(ListUtils.strList2String(templateSrvIds));

        Result<Boolean> result = clusterPhyService.editCluster(ConvertUtil.obj2Obj(clusterPhy, ESClusterDTO.class),
            operator);
        if (null != result && result.success()) {
            operateRecordService.save(CLUSTER, EDIT, phyCluster, phyCluster + "集群，删除一个索引服务：" + templateSrvId, operator);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> delTemplateSrvForClusterLogic(Long clusterLogicId, String templateSrvId, String operator) {
        if (!isRDOrOP(operator)) {
            return Result.buildNotExist(NO_PERMISSION_CONTENT);
        }

        if (Boolean.FALSE.equals(clusterLogicService.isClusterLogicExists(clusterLogicId))) {
            return Result.buildFail(CLUSTER_LOGIC_NOT_EXISTS);
        }

        ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(clusterLogicId);
        if (null == clusterLogicContext) {
            LOGGER.error(
                    "class=TemplateSrvManagerImpl||method=addTemplateSrvForClusterLogic||clusterLogicId={}||errMsg=failed to getClusterLogicContextFromCache",
                    clusterLogicId);
            return Result.buildFail();
        }

        List<String> associatedClusterPhyNames = clusterLogicContext.getAssociatedClusterPhyNames();
        if (CollectionUtils.isEmpty(associatedClusterPhyNames)) {
            return Result.buildSucc();
        }

        for (String associatedClusterPhyName : associatedClusterPhyNames) {
            try {
                Result<Boolean> ret = delTemplateSrv(associatedClusterPhyName, templateSrvId, operator);
                if (ret.failed()) {
                    throw new ESOperateException("逻辑集群删除索引服务失败");
                }
            } catch (ESOperateException e) {
                LOGGER.error(
                    "class=TemplateSrvManagerImpl||method=delTemplateSrvForClusterLogic||clusterLogicId={}||errMsg={}",
                    clusterLogicId, e.getMessage());
            }
        }

        return Result.buildSucc();

    }

    @Override
    public Result<Boolean> delAllTemplateSrvByClusterPhy(String clusterPhy, String operator) {
        if (!isRDOrOP(operator)) {
            return Result.buildNotExist(NO_PERMISSION_CONTENT);
        }

        ClusterPhy cluster = clusterPhyService.getClusterByName(clusterPhy);
        if (null == cluster) {
            return Result.buildNotExist(PHYSICAL_CLUSTER_NOT_EXISTS);
        }
        cluster.setTemplateSrvs("");
        Result<Boolean> result = clusterPhyService.editCluster(ConvertUtil.obj2Obj(cluster, ESClusterDTO.class),
                operator);
        if (result.success()) {
            operateRecordService.save(CLUSTER, DELETE, clusterPhy, clusterPhy + "物理集群绑定逻辑集群，删除索引服务：", operator);
        }

        return result;
    }

    /**************************************** private method ****************************************************/
    private ClusterTemplateSrv convertFromEnum(TemplateServiceEnum serviceEnum) {
        ClusterTemplateSrv clusterTemplateSrv = new ClusterTemplateSrv();
        clusterTemplateSrv.setServiceId(serviceEnum.getCode());
        clusterTemplateSrv.setServiceName(serviceEnum.getServiceName());
        clusterTemplateSrv.setEsVersion(serviceEnum.getEsClusterVersion().getVersion());

        return clusterTemplateSrv;
    }

    private boolean isRDOrOP(String operator) {
        return ariusUserInfoService.isRDByDomainAccount(operator) || ariusUserInfoService.isOPByDomainAccount(operator);
    }
}
