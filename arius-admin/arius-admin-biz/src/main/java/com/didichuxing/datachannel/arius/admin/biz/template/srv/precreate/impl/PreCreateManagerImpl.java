package com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDCDRManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusOpThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.TemplateConfig;

/**
 * @author chengxiang, zqr
 * @date 2022/5/11
 */
@Service
public class PreCreateManagerImpl extends BaseTemplateSrvImpl implements PreCreateManager {

    private final static Integer RETRY_TIMES  = 3;
    private final static Double  SUCCESS_RATE = 0.7;
    public static final String   START        = "*";

    @Autowired
    private TemplateDCDRManager  templateDcdrManager;

    @Autowired
    private ESIndexService       esIndexService;
    @Autowired
    private ESTemplateService    esTemplateService;

    @Autowired
    private AriusOpThreadPool    ariusOpThreadPool;

    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.TEMPLATE_PRE_CREATE;
    }
    

    @Override
    public Result<Boolean> preCreateIndex(Integer logicTemplateId) throws ESOperateException {
        if (Boolean.FALSE.equals(isTemplateSrvOpen(logicTemplateId))) {
            return Result.buildFail("指定索引模板未开启预先创建能力");
        }

        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);
        if (CollectionUtils.isEmpty(templatePhyList)) {
            LOGGER.info(
                "class=PreCreateManagerImpl||method=preCreateIndex||logicTemplateId={}||msg=PreCreateIndexTask no template",
                logicTemplateId);
            return Result.buildSucc();
        }

        Integer succeedCount = 0;
        for (IndexTemplatePhy templatePhy : templatePhyList) {
                if (syncCreateTomorrowIndexByPhysicalId(templatePhy.getId())) {
                    succeedCount++;
                } else {
                    LOGGER.warn(
                        "class=PreCreateManagerImpl||method=preCreateIndex||logicTemplateId={}||physicalTemplateId={}||msg=preCreateIndex fail",
                        logicTemplateId, templatePhy.getId());
                }
          
        }

        return Result.build(( succeedCount * 1.0 / templatePhyList.size() > SUCCESS_RATE));
    }

    @Override
    public void asyncCreateTodayAndTomorrowIndexByPhysicalId(Long physicalId) {
        ariusOpThreadPool.execute(() -> {
            try {
                //lbq这里睡眠一秒钟，保证上层方法数据事物已经提交；
                Thread.sleep(1000L);
                syncCreateTodayIndexByPhysicalId(physicalId);
                syncCreateTomorrowIndexByPhysicalId(physicalId);
            } catch (ESOperateException | InterruptedException e) {
                LOGGER.error(
                    "class=PreCreateManagerImpl||method=asyncCreateTodayIndexAsyncByPhysicalId||errMsg={}||physicalId={}",
                    e.getMessage(), physicalId, e);
            }
        });
    }

    ///////////////////////////////private method/////////////////////////////////////////////
    /**
     * 同步创建明天索引
     *
     * @param physicalId 物理模板id
     * @return result
     * @throws ESOperateException
     */
    private boolean syncCreateTomorrowIndexByPhysicalId(Long physicalId) throws ESOperateException {
        IndexTemplatePhyWithLogic physicalWithLogic = indexTemplatePhyService.getTemplateWithLogicById(physicalId);
        if (physicalWithLogic == null || !physicalWithLogic.hasLogic()) {
            return false;
        }

        // 如果是从模板不需要预先创建
        // 这里耦合了dcdr的逻辑，应该通过接口解耦
        if (physicalWithLogic.getRole().equals(TemplateDeployRoleEnum.SLAVE.getCode())
            && templateDcdrManager.clusterSupport(physicalWithLogic.getCluster())) {
            return true;
        }

        String tomorrowIndexName = IndexNameFactory.get(physicalWithLogic.getExpression(),
            physicalWithLogic.getLogicTemplate().getDateFormat(), 1, physicalWithLogic.getVersion());
        return createIndex(tomorrowIndexName, physicalWithLogic, RETRY_TIMES);
    }
    /**
     * 同步创建今天索引
     *
     * @param physicalId 物理模板id
     * @throws ESOperateException
     */
    @Override
    public boolean syncCreateTodayIndexByPhysicalId(Long physicalId, int version) throws ESOperateException {
        IndexTemplatePhyWithLogic physicalWithLogic = indexTemplatePhyService.getTemplateWithLogicById(physicalId);
        if (physicalWithLogic == null || !physicalWithLogic.hasLogic()) {
            return false;
        }
        String todayIndexName = IndexNameFactory.get(physicalWithLogic.getExpression(),
                physicalWithLogic.getLogicTemplate().getDateFormat(), 0, version);
        return createIndex(todayIndexName, physicalWithLogic, RETRY_TIMES);
    }

    /**
     * 同步创建今天索引
     * @param physicalId 物理模板id
     * @throws ESOperateException
     */
    private boolean syncCreateTodayIndexByPhysicalId(Long physicalId) throws ESOperateException {
        IndexTemplatePhyWithLogic physicalWithLogic = indexTemplatePhyService.getTemplateWithLogicById(physicalId);
        if (physicalWithLogic == null || !physicalWithLogic.hasLogic()) {
            return false;
        }
        String todayIndexName = IndexNameFactory.get(physicalWithLogic.getExpression(),
            physicalWithLogic.getLogicTemplate().getDateFormat(), 0, physicalWithLogic.getVersion());
        return createIndex(todayIndexName, physicalWithLogic, RETRY_TIMES);
    }

    private boolean createIndex(String indexName, IndexTemplatePhyWithLogic physicalWithLogic,
                                int retryCount) throws ESOperateException {
        IndexConfig indexConfig = null;
        if (!StringUtils.endsWith(physicalWithLogic.getExpression(), START) && physicalWithLogic.getVersion() > 0) {
            indexConfig = generateIndexConfig(physicalWithLogic);
        }
        if (null != indexConfig) {
            return esIndexService.syncCreateIndex(physicalWithLogic.getCluster(), indexName, indexConfig, retryCount);
        }
        return esIndexService.syncCreateIndex(physicalWithLogic.getCluster(), indexName, retryCount);
    }

    private IndexConfig generateIndexConfig(IndexTemplatePhyWithLogic physicalWithLogic) throws ESOperateException {
        TemplateConfig templateConfig = esTemplateService.syncGetTemplateConfig(physicalWithLogic.getCluster(),
                physicalWithLogic.getName());
        if (null == templateConfig) {
            throw new ESOperateException("获取模板配置失败，请稍后重试");
        }
        IndexConfig indexConfig = new IndexConfig();
        indexConfig.setMappings(templateConfig.getMappings());
        indexConfig.setSettings(templateConfig.getSetttings());
        indexConfig.setAliases(templateConfig.getAliases());
        indexConfig.setVersion(templateConfig.getVersion());
        return indexConfig;
    }

    /**
     * 同步删除明天索引
     * @param physicalId 物理模板id
     * @param retryCount 重试次数
     * @return
     * @throws ESOperateException
     */
    private boolean syncDeleteTomorrowIndexByPhysicalId(Long physicalId, int retryCount) throws ESOperateException {
        IndexTemplatePhyWithLogic physicalWithLogic = indexTemplatePhyService.getTemplateWithLogicById(physicalId);
        if (physicalWithLogic == null || !physicalWithLogic.hasLogic()) {
            return false;
        }

        String tomorrowIndexName = IndexNameFactory.get(physicalWithLogic.getExpression(),
            physicalWithLogic.getLogicTemplate().getDateFormat(), 1, physicalWithLogic.getVersion());
        String todayIndexName = IndexNameFactory.get(physicalWithLogic.getExpression(),
            physicalWithLogic.getLogicTemplate().getDateFormat(), 0, physicalWithLogic.getVersion());

        if (tomorrowIndexName.equals(todayIndexName)) {
            return false;
        }

        return esIndexService.syncDelIndex(physicalWithLogic.getCluster(), tomorrowIndexName, retryCount);
    }

    /////////////////////////////srv
    @Override
    public boolean preCreateIndex(String phyCluster, int retryCount) {
        

        List<IndexTemplatePhy> physicals = indexTemplatePhyService.getNormalTemplateByCluster(phyCluster);
        if (CollectionUtils.isEmpty(physicals)) {
            LOGGER.info(
                "class=ESClusterPhyServiceImpl||method=preCreateIndex||cluster={}||msg=PreCreateIndexTask no template",
                phyCluster);
            return true;
        }
       
        int succeedCount = 0;
        for (IndexTemplatePhy physical : physicals) {
            if (Boolean.FALSE.equals(isTemplateSrvOpen(physical.getLogicId()))) {
                continue;
            }
            
            IndexTemplateConfig config = indexTemplateService.getTemplateConfig(physical.getLogicId());
            if (config == null || !config.getPreCreateFlags()) {
                LOGGER.warn(
                    "class=ESClusterPhyServiceImpl||method=preCreateIndex||cluster={}||template={}||msg=skip preCreateIndex",
                    phyCluster, physical.getName());
                continue;
            }

            try {
                if (syncCreateTomorrowIndexByPhysicalId(physical.getId())) {
                    succeedCount++;
                } else {
                    LOGGER.warn(
                        "class=ESClusterPhyServiceImpl||method=preCreateIndex||cluster={}||template={}||msg=preCreateIndex fail",
                        phyCluster, physical.getName());
                }
            } catch (Exception e) {
                LOGGER.error("class=ESClusterPhyServiceImpl||method=preCreateIndex||errMsg={}||cluster={}||template={}",
                    e.getMessage(), phyCluster, physical.getName(), e);
            }
        }

        return succeedCount * 1.0 / physicals.size() > 0.7;
    }

    /**
     * 重建明天索引
     *
     * @param logicId    逻辑模板id
     * @param retryCount 重试次数
     * @return true/false
     */
    @Override
    public boolean reBuildTomorrowIndex(Integer logicId, int retryCount) throws ESOperateException {
        List<IndexTemplatePhy> indexTemplatePhies = indexTemplatePhyService.getTemplateByLogicId(logicId);
        if (CollectionUtils.isEmpty(indexTemplatePhies)) {
            return true;
        }

        boolean succ = true;
        for (IndexTemplatePhy indexTemplatePhy : indexTemplatePhies) {
            if (syncDeleteTomorrowIndexByPhysicalId(indexTemplatePhy.getId(), retryCount)) {
                succ = succ && syncCreateTomorrowIndexByPhysicalId(indexTemplatePhy.getId());
            }
        }

        return succ;
    }

}