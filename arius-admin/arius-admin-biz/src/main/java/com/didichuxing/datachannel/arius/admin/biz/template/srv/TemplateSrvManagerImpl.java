package com.didichuxing.datachannel.arius.admin.biz.template.srv;

import java.util.*;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrvInterface;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterTemplateSrv;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
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

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.CLUSTER;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.DELETE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;

@Service("templateSrvService")
@DependsOn("springTool")
public class TemplateSrvManagerImpl implements TemplateSrvManager {
    protected static final ILog   LOGGER                      = LogFactory.getLog(TemplateSrvManagerImpl.class);

    @Autowired
    private ClusterPhyService     clusterPhyService;

    @Autowired
    private ClusterLogicService   clusterLogicService;

    @Autowired
    private ClusterRegionService  clusterRegionService;

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

    Map<Integer, BaseTemplateSrvInterface> templateHandlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        LOGGER.info("class=TemplateSrvManagerImpl||method=init||TemplateSrvManagerImpl init start.");
        Map<String, BaseTemplateSrvInterface> strTemplateHandlerMap = SpringTool.getBeansOfType(BaseTemplateSrvInterface.class);

        strTemplateHandlerMap.forEach((key, val) -> {
            try {
                TemplateServiceEnum templateServiceEnum = val.templateService();

                if (null != templateServiceEnum) {
                    templateHandlerMap.put(templateServiceEnum.getCode(), val);

                    LOGGER.warn("class=TemplateSrvManager||method=init||templateSrvName={}||esVersion={}",
                        templateServiceEnum.getServiceName(), templateServiceEnum.getEsClusterVersion());
                }
            } catch (Exception e) {
                LOGGER.warn("class=TemplateSrvManager||method=init||templateSrvName={}||esVersion={}", key);
            }
        });
        LOGGER.info("class=TemplateSrvManagerImpl||method=init||TemplateSrvManagerImpl init finished.");
    }

    @Override
    public ClusterTemplateSrv getTemplateServiceBySrvId(int srvId) {
        return convertFromEnum(TemplateServiceEnum.getById(srvId));
    }

    @Override
    public List<String> getPhyClusterByOpenTemplateSrv(int srvId) {
        List<ClusterPhy> clusterPhies = clusterPhyService.listAllClusters();
        return getPhyClusterByOpenTemplateSrv(clusterPhies, srvId);
    }

    @Override
    public List<String> getPhyClusterByOpenTemplateSrv(List<ClusterPhy> clusterPhies, int srvId) {
        List<String> clusterPhyNames = new ArrayList<>();
        if (CollectionUtils.isEmpty(clusterPhies)) {
            return clusterPhyNames;
        }
        clusterPhies.forEach(clusterPhy -> {
            if (isPhyClusterOpenTemplateSrv(clusterPhy, srvId)) {
                clusterPhyNames.add(clusterPhy.getCluster());
            }
        });
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
    public boolean isPhyClusterOpenTemplateSrv(ClusterPhy phyCluster, int srvId) {
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
        return getPhyClusterTemplateSrv(clusterPhy);
    }

    @Override
    public Result<List<ClusterTemplateSrv>> getPhyClusterTemplateSrv(ClusterPhy clusterPhy) {
        if (null == clusterPhy) {
            return Result.buildNotExist(PHYSICAL_CLUSTER_NOT_EXISTS);
        }

        String templateSrvs = clusterPhy.getTemplateSrvs();
        if (StringUtils.isBlank(templateSrvs)) {
            return Result.buildSucc(new ArrayList<>(), "该物理集群无索引服务");
        }

        List<ClusterTemplateSrv> templateServices = new ArrayList<>();
        for (String strId : StringUtils.split(templateSrvs, COMMA)) {
            ClusterTemplateSrv templateSrv = getTemplateServiceBySrvId(Integer.parseInt(strId));
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
        List<String> phyClusterNames = clusterRegionService.listPhysicClusterNames(logicClusterId);
        if (CollectionUtils.isEmpty(phyClusterNames)) {
            return Result.buildNotExist("逻辑集群对应的物理集群不存在");
        }

        Set<ClusterTemplateSrv> templateServiceTotals = new HashSet<>();

        for (String phyClusterName : phyClusterNames) {
            Result<List<ClusterTemplateSrv>> templateSrvsRet = getPhyClusterTemplateSrv(phyClusterName);
            if (null != templateSrvsRet && templateSrvsRet.success()) {
                templateServiceTotals.addAll(templateSrvsRet.getData());
            }
        }

        return Result.buildSucc(new ArrayList<>(templateServiceTotals));
    }

    @Override
    public Result<Boolean> checkTemplateSrv(String phyCluster, String templateSrvId, String operator) {
        ClusterTemplateSrv clusterTemplateSrv = getTemplateServiceBySrvId(Integer.parseInt(templateSrvId));
        if (null == clusterTemplateSrv) {
            return Result.buildNotExist("对应的索引服务不存在");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyCluster);
        if (null == clusterPhy) {
            return Result.buildNotExist(PHYSICAL_CLUSTER_NOT_EXISTS);
        }

        //对模板服务的开启做校验
        Result<Boolean> validResult = validCanOpenTemplateSrvId(phyCluster, templateSrvId);
        if (validResult.failed()) {
            return Result.buildFrom(validResult);
        }

        if (StringUtils.isBlank(clusterPhy.getTemplateSrvs())) {
            clusterPhy.setTemplateSrvs(templateSrvId);
        } else {
            List<String> templateSrvs = ListUtils.string2StrList(clusterPhy.getTemplateSrvs());
            if (!templateSrvs.contains(templateSrvId)) {
                //增加模板服务的开启校验，具体的逻辑映射到具体的模板服务当中
                clusterPhy.setTemplateSrvs(clusterPhy.getTemplateSrvs() + "," + templateSrvId);
            }else {
                return Result.buildSucc();
            }
        }

        Result<Boolean> result = clusterPhyService.editCluster(ConvertUtil.obj2Obj(clusterPhy, ClusterPhyDTO.class),
            operator);
        if (result.success()) {
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
                Result<Boolean> ret = checkTemplateSrv(associatedClusterPhyName, templateSrvId, operator);
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
        return clusterPhyService.editCluster(ConvertUtil.obj2Obj(clusterPhy, ClusterPhyDTO.class), operator);

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

        Result<Boolean> result = clusterPhyService.editCluster(ConvertUtil.obj2Obj(clusterPhy, ClusterPhyDTO.class),
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
        Result<Boolean> result = clusterPhyService.editCluster(ConvertUtil.obj2Obj(cluster, ClusterPhyDTO.class),
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

    /**
     * 根据物理集群名称和模板服务的映射id校验是否能够开启指定的模板服务
     * @param phyCluster 物理集群名称
     * @param templateSrvId 模板服务id
     * @return 校验结果
     */
    private Result<Boolean> validCanOpenTemplateSrvId(String phyCluster, String templateSrvId) {
        TemplateServiceEnum templateServiceEnum = TemplateServiceEnum.getById(Integer.parseInt(templateSrvId));
        if (templateServiceEnum == null ||
                AriusObjUtils.isNull(templateHandlerMap.get(Integer.parseInt(templateSrvId)))) {
            return Result.buildFail("指定模板服务id有误");
        }

        return templateHandlerMap.get(Integer.parseInt(templateSrvId)).checkOpenTemplateSrvByCluster(phyCluster);
    }
}
