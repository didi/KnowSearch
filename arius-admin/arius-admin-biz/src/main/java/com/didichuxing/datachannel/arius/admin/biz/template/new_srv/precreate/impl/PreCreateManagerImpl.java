package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.precreate.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.precreate.PreCreateManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDCDRManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.BaseTemplateSrvOpenDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusOpThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chengxiang, zqr
 * @date 2022/5/11
 */
@Service
public class PreCreateManagerImpl extends BaseTemplateSrvImpl implements PreCreateManager {

    private final Integer RETRY_TIMES = 3;
    private final Double SUCCESS_RATE = 0.7;

    @Autowired
    private TemplateDCDRManager templateDcdrManager;

    @Autowired
    private ESIndexService esIndexService;

    @Autowired
    private AriusOpThreadPool ariusOpThreadPool;

    @Override
    public NewTemplateSrvEnum templateSrv() {
        return NewTemplateSrvEnum.TEMPLATE_PRE_CREATE;
    }

    @Override
    public Result<Void> isTemplateSrvAvailable(Integer logicTemplateId) {
        return Result.buildSucc();
    }

    @Override
    protected Result<Void> openSrvImpl(List<Integer> templateIdList, BaseTemplateSrvOpenDTO openParam) {
        return Result.buildFail();
    }

    @Override
    protected Result<Void> closeSrvImpl(List<Integer> templateIdList) {
        return Result.buildFail();
    }

    @Override
    public Result<Void> preCreateIndex(Integer logicTemplateId) {
        if (!isTemplateSrvOpen(logicTemplateId)) {
            return Result.buildFail("指定索引模板未开启预先创建能力");
        }

        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);
        if (CollectionUtils.isEmpty(templatePhyList)) {
            LOGGER.info("class=PreCreateManagerImpl||method=preCreateIndex||logicTemplateId={}||msg=PreCreateIndexTask no template", logicTemplateId);
            return Result.buildSucc();
        }

        Integer succeedCount = 0;
        for (IndexTemplatePhy templatePhy: templatePhyList) {
            try {
                if (syncCreateTomorrowIndexByPhysicalId(templatePhy.getId(), RETRY_TIMES)) {
                    succeedCount++;
                } else {
                    LOGGER.warn("class=PreCreateManagerImpl||method=preCreateIndex||logicTemplateId={}||physicalTemplateId={}||msg=preCreateIndex fail", logicTemplateId, templatePhy.getId());
                }
            } catch (Exception e) {
                LOGGER.error("class=PreCreateManagerImpl||method=preCreateIndex||errMsg={}||logicTemplate={}||physicalTemplate={}", e.getMessage(), logicTemplateId, templatePhy.getId(), e);
            }
        }

        return succeedCount * 1.0 / templatePhyList.size() > SUCCESS_RATE ? Result.buildSucc() : Result.buildFail("预创建失败");
    }

    @Override
    public Result<Void> reBuildTomorrowIndex(Integer logicTemplateId) {
        List<IndexTemplatePhy> templatePhyList = indexTemplatePhyService.getTemplateByLogicId(logicTemplateId);
        if (CollectionUtils.isEmpty(templatePhyList)) {
            return Result.buildSucc();
        }

        Boolean succ = Boolean.TRUE;
        for (IndexTemplatePhy templatePhy : templatePhyList) {
            Long phyTemplateId = templatePhy.getId();
            try {
                if (syncDeleteTomorrowIndexByPhysicalId(phyTemplateId, RETRY_TIMES)) {
                    succ = succ && syncCreateTomorrowIndexByPhysicalId(phyTemplateId, RETRY_TIMES);
                }
            } catch (Exception e) {
                LOGGER.error("class=PreCreateManagerImpl||method=reBuildTomorrowIndex||errMsg={}||logicTemplate={}||physicalTemplate={}", e.getMessage(), logicTemplateId, phyTemplateId, e);
            }
        }
        return succ ? Result.buildSucc() : Result.buildFail("重建明天索引失败");
    }

    @Override
    public void asyncCreateTodayAndTomorrowIndexByPhysicalId(Long physicalId) {
        ariusOpThreadPool.execute(() -> {
            try {
                syncCreateTomorrowIndexByPhysicalId(physicalId, RETRY_TIMES);
                syncCreateTodayIndexByPhysicalId(physicalId, RETRY_TIMES);
            } catch (ESOperateException e) {
                LOGGER.error("class=PreCreateManagerImpl||method=asyncCreateTodayIndexAsyncByPhysicalId||errMsg={}||physicalId={}", e.getMessage(), physicalId, e);
            }
        });
    }



    ///////////////////////////////private method/////////////////////////////////////////////
    /**
     * 同步创建明天索引
     *
     * @param physicalId 物理模板id
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    private boolean syncCreateTomorrowIndexByPhysicalId(Long physicalId, int retryCount) throws ESOperateException {
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

        String tomorrowIndexName = IndexNameFactory.getNoVersion(physicalWithLogic.getExpression(),
                physicalWithLogic.getLogicTemplate().getDateFormat(), 1);
        return esIndexService.syncCreateIndex(physicalWithLogic.getCluster(), tomorrowIndexName, retryCount);
    }

    /**
     * 同步创建今天索引
     * @param physicalId 物理模板id
     * @param retryCount 重试次数
     * @throws ESOperateException
     */
    private boolean syncCreateTodayIndexByPhysicalId(Long physicalId, int retryCount) throws ESOperateException {
        IndexTemplatePhyWithLogic physicalWithLogic = indexTemplatePhyService.getTemplateWithLogicById(physicalId);
        if (physicalWithLogic == null || !physicalWithLogic.hasLogic()) {
            return false;
        }
        String todayIndexName = IndexNameFactory.get(physicalWithLogic.getExpression(),
                physicalWithLogic.getLogicTemplate().getDateFormat(), 0, physicalWithLogic.getVersion());
        return esIndexService.syncCreateIndex(physicalWithLogic.getCluster(), todayIndexName, retryCount);
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


}
