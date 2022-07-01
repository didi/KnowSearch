package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.TEMPLATE_SRV;

import com.didichuxing.datachannel.arius.admin.biz.page.TemplateSrvPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.UnavailableTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateWithSrvVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * @author chengxiang
 * @date 2022/5/9
 */
@Service("newTemplateSrvService")
@DependsOn("springTool")
public class TemplateSrvManagerImpl implements TemplateSrvManager {
    protected static final ILog LOGGER = LogFactory.getLog(TemplateSrvManagerImpl.class);
    /**
     * 超级应用，仅运维使用
     */
    private  static final Integer SUPER_APP_ID = 1;
    private final Map<Integer, BaseTemplateSrv> BASE_TEMPLATE_SRV_MAP = Maps.newConcurrentMap();

    /**
     * 本地cache 加快无效索引服务过滤
     */
    private static final Cache<Integer, String/*ESClusterVersionEnum*/> LOGIC_TEMPLATE_ID_2_ASSOCIATED_CLUSTER_VERSION_ENUM_CACHE
            = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(10000).build();

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private ClusterPhyService   clusterPhyService;

    @Autowired
    private HandleFactory       handleFactory;

    @PostConstruct
    public void init() {
        Map<String, BaseTemplateSrv> strTemplateSrvHandleMap = SpringTool.getBeansOfType(BaseTemplateSrv.class);
        strTemplateSrvHandleMap.forEach((k, v) -> {
            try {
                NewTemplateSrvEnum srvEnum = v.templateSrv();
                BASE_TEMPLATE_SRV_MAP.put(srvEnum.getCode(), v);
            } catch (Exception e) {
                LOGGER.error("class=TemplateSrvManagerImpl||method=init||error=", e);
            }
        });
        LOGGER.info("class=TemplateSrvManagerImpl||method=init||init finish");
    }

    @Override
    public Result<List<TemplateSrv>> getTemplateOpenSrv(Integer logicTemplateId) {
        try {
            IndexTemplate template = indexTemplateService.getLogicTemplateById(logicTemplateId);
            if (null == template) {
                return Result.buildNotExist("逻辑模板不存在");
            }

            return Result.buildSucc(TemplateSrv.codeStr2SrvList(template.getOpenSrv()));
        } catch (Exception e) {
            LOGGER.error("class=TemplateSrvManagerImpl||method=getTemplateOpenSrv||logicTemplateId={}", logicTemplateId, e);
            return Result.buildFail( "获取模板开启服务失败");
        }
    }

    @Override
    public boolean isTemplateSrvOpen(Integer logicTemplateId, Integer srvCode) {
        Result<List<TemplateSrv>> openSrvResult = getTemplateOpenSrv(logicTemplateId);
        if (openSrvResult.failed()) {
            return false;
        }

        List<TemplateSrv> openSrv = openSrvResult.getData();
        for (TemplateSrv srv : openSrv) {
            if (srvCode.equals(srv.getSrvCode())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<UnavailableTemplateSrv> getUnavailableSrv(Integer logicTemplateId) {
        List<UnavailableTemplateSrv> unavailableSrvList = Lists.newCopyOnWriteArrayList();
        List<NewTemplateSrvEnum> allSrvList = NewTemplateSrvEnum.getAll();
        for (NewTemplateSrvEnum srvEnum : allSrvList) {
            String esVersionFromESCluster = getLogicTemplateAssociatedEsVersionByLogicTemplateId(logicTemplateId);
            if (ESVersionUtil.isHigher(srvEnum.getEsClusterVersion().getVersion(), esVersionFromESCluster)) {
                unavailableSrvList.add(new UnavailableTemplateSrv(
                        srvEnum.getCode(),
                        srvEnum.getServiceName(),
                        srvEnum.getEsClusterVersion().getVersion(),
                        String.format("不支持该模板服务, 模板[%s]归属集群目前版本[%s], 模板服务需要的最低版本为[%s]",
                                logicTemplateId, esVersionFromESCluster, srvEnum.getEsClusterVersion().getVersion())));
            }
        }
        return unavailableSrvList;
    }

    @Override
    public PaginationResult<TemplateWithSrvVO> pageGetTemplateWithSrv(TemplateQueryDTO condition) {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(TEMPLATE_SRV.getPageSearchType());
        if (baseHandle instanceof TemplateSrvPageSearchHandle) {
            TemplateSrvPageSearchHandle handler = (TemplateSrvPageSearchHandle) baseHandle;
            return handler.doPage(condition,condition.getProjectId() );
        }
        return PaginationResult.buildFail("没有找到对应的处理器");
    }

    @Override
    public Result<Void> openSrv(Integer srvCode, List<Integer> templateIdList, String operator, Integer projectId) {
        BaseTemplateSrv srvHandle = BASE_TEMPLATE_SRV_MAP.get(srvCode);
        if (null == srvHandle) { return Result.buildParamIllegal("未找到对应的服务");}

        try {
            return srvHandle.openSrv(templateIdList,operator,projectId);
        } catch (AdminOperateException e) {
            LOGGER.error("class=TemplateSrvManagerImpl||method=openSrv||templateIdList={}||srvCode={}" +
                    "||errMsg=failed to open template srv", ListUtils.intList2String(templateIdList), srvCode);
            return Result.buildFail(e.getMessage());
        }
    }

    @Override
    public Result<Void> closeSrv(Integer srvCode, List<Integer> templateIdList, String operator, Integer projectId) {
        BaseTemplateSrv srvHandle = BASE_TEMPLATE_SRV_MAP.get(srvCode);
        if (null == srvHandle) { return Result.buildParamIllegal("未找到对应服务");}

        try {
            return srvHandle.closeSrv(templateIdList,operator,projectId);
        } catch (AdminOperateException e) {
            LOGGER.error("class=TemplateSrvManagerImpl||method=closeSrv||templateIdList={}||srvCode={}" +
                    "||errMsg=failed to open template srv", ListUtils.intList2String(templateIdList), srvCode);
            return Result.buildFail(e.getMessage());
        }
    }


    private String getLogicTemplateAssociatedEsVersionByLogicTemplateId(Integer logicTemplateId) {
        try {
            return LOGIC_TEMPLATE_ID_2_ASSOCIATED_CLUSTER_VERSION_ENUM_CACHE.get(logicTemplateId,
                    () -> {
                        IndexTemplateLogicWithClusterAndMasterTemplate template = indexTemplateService.getLogicTemplateWithClusterAndMasterTemplate(logicTemplateId);
                        if (null == template || null == template.getMasterTemplate()) {
                            LOGGER.warn("class=TemplateSrvPageSearchHandle||method=getLogicTemplateAssociatedEsVersionByLogicTemplateId" +
                                            "||templateId={}||errMsg=masterPhyTemplate is null",
                                    logicTemplateId);
                            return "";
                        }

                        String masterCluster = template.getMasterTemplate().getCluster();
                        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(masterCluster);
                        if (null == clusterPhy) {
                            LOGGER.warn("class=TemplateSrvPageSearchHandle||method=getLogicTemplateAssociatedEsVersionByLogicTemplateId" +
                                            "||templateId={}||errMsg=clusterPhy of template is null",
                                    logicTemplateId);
                            return "";
                        }

                        return clusterPhy.getEsVersion();
                    });
        } catch (ExecutionException e) {
            LOGGER.error("class=TemplateSrvPageSearchHandle||method=getLogicTemplateAssociatedEsVersionByLogicTemplateId" +
                            "||templateId={}||errMsg={}",
                    logicTemplateId, e.getMessage(), e);
            return "";
        }
    }
}