package com.didichuxing.datachannel.arius.admin.biz.template.srv;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.CLUSTER;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EDIT;

@Service("templateSrvService")
@DependsOn("springTool")
public class TemplateSrvManagerImpl implements TemplateSrvManager {
    protected static final ILog   LOGGER             = LogFactory.getLog(TemplateSrvManagerImpl.class);

    @Autowired
    private ESClusterPhyService   esClusterPhyService;

    @Autowired
    private ESRegionRackService   esClusterRackService;

    @Autowired
    private OperateRecordService  operateRecordService;

    @Autowired
    private AriusUserInfoService  ariusUserInfoService;

    private final static String   COMMA              = ",";

    Map<Integer, BaseTemplateSrv> templateHandlerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        LOGGER.info("class=TemplateSrvManagerImpl||method=init||TemplateSrvManagerImpl init start.");
        Map<String, BaseTemplateSrv> strTemplateHandlerMap = SpringTool.getBeansOfType(BaseTemplateSrv.class);

        for (String str : strTemplateHandlerMap.keySet()) {
            BaseTemplateSrv baseTemplateHandler = strTemplateHandlerMap.get(str);
            TemplateServiceEnum templateServiceEnum = baseTemplateHandler.templateService();

            if (null != templateServiceEnum) {
                templateHandlerMap.put(templateServiceEnum.getCode(), baseTemplateHandler);

                LOGGER.warn("class=TemplateSrvManager||method=init||templateSrvName={}||esVersion={}",
                    templateServiceEnum.getServiceName(), templateServiceEnum.getEsClusterVersion());
            }
        }
        LOGGER.info("class=TemplateSrvManagerImpl||method=init||TemplateSrvManagerImpl init finished.");
    }

    @Override
    public ESClusterTemplateSrv getTemplateServiceBySrvId(int srvId) {
        return convertFromEnum(TemplateServiceEnum.getById(srvId));
    }

    @Override
    public boolean isPhyClusterOpenTemplateSrv(String phyCluster, int srvId) {
        try {
            Result<List<ESClusterTemplateSrv>> result = getPhyClusterTemplateSrv(phyCluster);
            if (null == result || result.failed()) {
                return false;
            }

            List<ESClusterTemplateSrv> esClusterTemplateSrvs = result.getData();
            for (ESClusterTemplateSrv templateSrv : esClusterTemplateSrvs) {
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
    public Result<List<ESClusterTemplateSrv>> getPhyClusterSelectableTemplateSrv(String phyCluster) {
        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterByName(phyCluster);
        if (null == esClusterPhy) {
            return Result.buildNotExist("物理集群不存在");
        }

        List<ESClusterTemplateSrv> templateServices = new ArrayList<>();
        String clusterVersion = esClusterPhy.getEsVersion();

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
    public Result<List<ESClusterTemplateSrv>> getPhyClusterTemplateSrv(String phyCluster) {
        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterByName(phyCluster);
        if (null == esClusterPhy) {
            return Result.buildNotExist("物理集群不存在");
        }

        String templateSrvs = esClusterPhy.getTemplateSrvs();
        if (StringUtils.isBlank(templateSrvs)) {
            return Result.buildSucc(new ArrayList<>(), "该物理集群无索引服务");
        }

        List<ESClusterTemplateSrv> templateServices = new ArrayList<>();
        for (String strId : StringUtils.split(templateSrvs, COMMA)) {
            ESClusterTemplateSrv templateSrv = getTemplateServiceBySrvId(Integer.valueOf(strId));
            if (null != templateSrv) {
                templateServices.add(templateSrv);
            }
        }

        return Result.buildSucc(templateServices);
    }

    @Override
    public Result<List<ESClusterTemplateSrv>> getLogicClusterTemplateSrv(Long logicClusterId) {
        List<String> phyClusterNames = esClusterRackService.listPhysicClusterNames(logicClusterId);
        if (CollectionUtils.isEmpty(phyClusterNames)) {
            return Result.buildNotExist("逻辑集群对应的物理集群不存在");
        }

        Set<ESClusterTemplateSrv> templateServiceTotals = new HashSet<>();

        for (String phyClusterName : phyClusterNames) {
            Result<List<ESClusterTemplateSrv>> templateSrvsRet = getPhyClusterTemplateSrv(phyClusterName);
            if (null != templateSrvsRet && templateSrvsRet.success()) {
                for (ESClusterTemplateSrv templateSrv : templateSrvsRet.getData()) {
                    templateServiceTotals.add(templateSrv);
                }
            }
        }

        return Result.buildSucc(new ArrayList<>(templateServiceTotals));
    }

    @Override
    public Result<Boolean> addTemplateSrv(String phyCluster, String templateSrvId, String operator) {
        if (!isRDOrOP(operator)) {
            return Result.buildNotExist("只有运维或者研发才有权限操作");
        }

        ESClusterTemplateSrv esClusterTemplateSrv = getTemplateServiceBySrvId(Integer.valueOf(templateSrvId));
        if (null == esClusterTemplateSrv) {
            return Result.buildNotExist("对应的索引服务不存在");
        }

        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterByName(phyCluster);
        if (null == esClusterPhy) {
            return Result.buildNotExist("物理集群不存在");
        }

        if (StringUtils.isBlank(esClusterPhy.getTemplateSrvs())) {
            esClusterPhy.setTemplateSrvs(templateSrvId);
        } else {
            esClusterPhy.setTemplateSrvs(esClusterPhy.getTemplateSrvs() + "," + templateSrvId);
        }

        Result result = esClusterPhyService.editCluster(ConvertUtil.obj2Obj(esClusterPhy, ESClusterDTO.class),
            operator);
        if (null != result && result.success()) {
            operateRecordService.save(CLUSTER, EDIT, phyCluster,
                phyCluster + "集群，增加一个索引服务：" + esClusterTemplateSrv.getServiceName(), operator);
        }
        return result;
    }

    @Override
    public Result<Boolean> delTemplateSrv(String phyCluster, String templateSrvId, String operator) {
        if (!isRDOrOP(operator)) {
            return Result.buildNotExist("只有运维或者研发才有权限操作");
        }

        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterByName(phyCluster);
        if (null == esClusterPhy) {
            return Result.buildNotExist("物理集群不存在");
        }

        String[] templateSrvIds = StringUtils.split(esClusterPhy.getTemplateSrvs(), COMMA);
        if (null == templateSrvIds || templateSrvIds.length == 0) {
            return Result.buildNotExist("物理集群的索引服务为空");
        }

        List<String> srvIds = Arrays.stream(templateSrvIds).collect(Collectors.toList());
        if (!srvIds.contains(templateSrvId)) {
            return Result.buildNotExist("物理集群现有的索引服务不包含即将删除的索引服务");
        }

        srvIds.remove(templateSrvId);
        esClusterPhy.setTemplateSrvs(StringUtils.join(srvIds, COMMA));

        Result result = esClusterPhyService.editCluster(ConvertUtil.obj2Obj(esClusterPhy, ESClusterDTO.class),
            operator);
        if (null != result && result.success()) {
            operateRecordService.save(CLUSTER, EDIT, phyCluster, phyCluster + "集群，删除一个索引服务：" + templateSrvId, operator);
        }
        return result;
    }

    /**************************************** private method ****************************************************/
    private ESClusterTemplateSrv convertFromEnum(TemplateServiceEnum serviceEnum) {
        ESClusterTemplateSrv esClusterTemplateSrv = new ESClusterTemplateSrv();
        esClusterTemplateSrv.setServiceId(serviceEnum.getCode());
        esClusterTemplateSrv.setServiceName(serviceEnum.getServiceName());
        esClusterTemplateSrv.setEsVersion(serviceEnum.getEsClusterVersion().getVersion());

        return esClusterTemplateSrv;
    }

    private boolean isRDOrOP(String operator) {
        return ariusUserInfoService.isRDByDomainAccount(operator) || ariusUserInfoService.isOPByDomainAccount(operator);
    }
}
