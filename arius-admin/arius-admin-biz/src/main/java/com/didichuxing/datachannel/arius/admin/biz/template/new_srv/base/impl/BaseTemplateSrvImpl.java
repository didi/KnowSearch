package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.BaseTemplateSrvOpenDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.constant.ESClusterVersionEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author chengxiang
 * @date 2022/5/11
 */
public abstract class BaseTemplateSrvImpl implements BaseTemplateSrv {
    protected static final ILog LOGGER = LogFactory.getLog(BaseTemplateSrvImpl.class);

    @Autowired
    protected IndexTemplateService    indexTemplateService;

    @Autowired
    protected IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    protected TemplatePhyManager      templatePhyManager;

    @Autowired
    protected TemplateSrvManager      templateSrvManager;

    @Autowired
    private ClusterPhyService         clusterPhyService;
    @Override
    public boolean isTemplateSrvOpen(Integer templateId) {
        return templateSrvManager.isTemplateSrvOpen(templateId, templateSrv().getCode());
    }

    @Override
    public String templateSrvName() {
        return templateSrv().getServiceName();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> openSrv(List<Integer> templateIdList, BaseTemplateSrvOpenDTO openParam) throws AdminOperateException {
        // 0.校验服务是否可以开启
        for (Integer templateId : templateIdList) {
            Result<Void> checkAvailableResult = checkSrvIsValid(templateId);
            if (checkAvailableResult.failed()) { return checkAvailableResult;}
        }

        // 1.更新DB服务开启状态
        Result<Void> updateResult = updateSrvStatus(templateIdList, Boolean.TRUE);
        if (updateResult.failed()) { return updateResult;}

        return Result.buildSucc();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> closeSrv(List<Integer> templateIdList) throws AdminOperateException {
        // 0.更新DB服务关闭状态
        Result<Void> updateResult = updateSrvStatus(templateIdList, Boolean.FALSE);
        if (updateResult.failed()) { return updateResult;}

        return Result.buildSucc();
    }

    protected Result<Void> checkSrvIsValid(Integer logicTemplateId) {
        ESClusterVersionEnum requireESClusterVersion = templateSrv().getEsClusterVersion();

        IndexTemplateLogicWithClusterAndMasterTemplate template = indexTemplateService.getLogicTemplateWithClusterAndMasterTemplate(logicTemplateId);
        if (null == template || null == template.getMasterTemplate()) {
            LOGGER.warn("class=ColdManagerImpl||method=isTemplateSrvAvailable||templateId={}||errMsg=masterPhyTemplate is null",
                    logicTemplateId);
            return Result.buildFail();
        }

        String masterCluster = template.getMasterTemplate().getCluster();
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(masterCluster);
        if (null == clusterPhy) {
            LOGGER.warn("class=ColdManagerImpl||method=isTemplateSrvAvailable||templateId={}||errMsg=clusterPhy of template is null",
                    logicTemplateId);
            return Result.buildFail();
        }

        String esVersion = clusterPhy.getEsVersion();

        if (ESVersionUtil.isHigher(requireESClusterVersion.getVersion(), esVersion)) {
            return Result.buildFail(String.format("不支持该模板服务, 模板[%s]归属集群目前版本为:%s, 模板服务需要的最低版本为:%s",
                    logicTemplateId,esVersion, requireESClusterVersion.getVersion()));
        }
        return Result.buildSucc();
    }

    /******************************************private************************************************/
    /**
     * 更新DB服务状态
     * @param templateIdList
     * @param status, true:开启, false:关闭
     * @return
     */
    private Result<Void> updateSrvStatus(List<Integer> templateIdList, Boolean status) throws AdminOperateException {
        String srvCode = templateSrv().getCode().toString();
        for (Integer templateId : templateIdList) {
            IndexTemplate indexTemplate = indexTemplateService.getLogicTemplateById(templateId);
            if (null == indexTemplate) { continue;}

            if (status) { addSrvCode(indexTemplate, srvCode);}
            else { removeSrvCode(indexTemplate, srvCode);}

            indexTemplateService.editTemplateInfoTODB(ConvertUtil.obj2Obj(indexTemplate, IndexTemplateDTO.class));
        }
        return Result.buildSucc();
    }

    /**
     * 添加开启服务到对应模板实体中
     * @param indexTemplate
     * @param addSrvCode
     * @return true:有修改, false:无修改；根据返回值判断是否需要刷新到DB
     */
    private void addSrvCode(IndexTemplate indexTemplate, String addSrvCode) {
        String srvCodeStr = indexTemplate.getOpenSrv();
        List<String> srvCodeList = ListUtils.string2StrList(srvCodeStr);
        if (srvCodeList.isEmpty()) {
            indexTemplate.setOpenSrv(addSrvCode);
        } else {
            if (!srvCodeList.contains(addSrvCode)) {
                indexTemplate.setOpenSrv(srvCodeStr + "," + addSrvCode);
            }
        }
    }

    private void removeSrvCode(IndexTemplate indexTemplate, String removeSrvCode) {
        String srvCodeStr = indexTemplate.getOpenSrv();
        List<String> srvCodeList = ListUtils.string2StrList(srvCodeStr);
        if (srvCodeList.contains(removeSrvCode)) {
            srvCodeList.remove(removeSrvCode);
            indexTemplate.setOpenSrv(ListUtils.strList2String(srvCodeList));
        }
    }
    
}
