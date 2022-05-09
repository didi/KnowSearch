package com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDcdrManager;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusOpThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_PRE_CREATE;

/**
 * 索引预创建服务实现
 * @author zqr
 * @date 2020-09-09
 */
@Service
public class TemplatePreCreateManagerImpl extends BaseTemplateSrv implements TemplatePreCreateManager {

    @Autowired
    private TemplateDcdrManager templateDcdrManager;

    @Autowired
    private ESIndexService           esIndexService;

    @Autowired
    private AriusOpThreadPool        ariusOpThreadPool;

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_PRE_CREATE;
    }

    @Override
    public boolean preCreateIndex(String phyCluster, int retryCount) {
        if (!isTemplateSrvOpen(phyCluster)) {
            return false;
        }

        List<IndexTemplatePhy> physicals = templatePhyService.getNormalTemplateByCluster(phyCluster);
        if (CollectionUtils.isEmpty(physicals)) {
            LOGGER.info(
                "class=ESClusterPhyServiceImpl||method=preCreateIndex||cluster={}||msg=PreCreateIndexTask no template",
                phyCluster);
            return true;
        }

        int succeedCount = 0;
        for (IndexTemplatePhy physical : physicals) {
            IndexTemplateConfig config = indexTemplateInfoService.getTemplateConfig(physical.getLogicId());
            if (config == null || !config.getPreCreateFlags()) {
                LOGGER.warn(
                    "class=ESClusterPhyServiceImpl||method=preCreateIndex||cluster={}||template={}||msg=skip preCreateIndex",
                    phyCluster, physical.getName());
                continue;
            }

            try {
                if (syncCreateTomorrowIndexByPhysicalId(physical.getId(), retryCount)) {
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
        List<IndexTemplatePhy> indexTemplatePhys = templatePhyService.getTemplateByLogicId(logicId);
        if (CollectionUtils.isEmpty(indexTemplatePhys)) {
            return true;
        }

        boolean succ = true;
        for (IndexTemplatePhy indexTemplatePhy : indexTemplatePhys) {
            if (syncDeleteTomorrowIndexByPhysicalId(indexTemplatePhy.getId(), retryCount)) {
                succ = succ && syncCreateTomorrowIndexByPhysicalId(indexTemplatePhy.getId(), retryCount);
            }
        }

        return succ;
    }

    /**
     * 异步创建今天索引
     * @param physicalId 物理模板id
     * @param retryCount 重试次数
     */
    @Override
    public void asyncCreateTodayAndTomorrowIndexByPhysicalId(Long physicalId, int retryCount) {
        ariusOpThreadPool.execute(() -> {
            try {
                syncCreateTomorrowIndexByPhysicalId(physicalId, retryCount);
                syncCreateTodayIndexByPhysicalId(physicalId, retryCount);
            } catch (ESOperateException e) {
                LOGGER.error(
                    "class=ESIndexServiceImpl||method=asyncCreateTodayIndexAsyncByPhysicalId||errMsg={}||physicalId={}",
                    e.getMessage(), physicalId, e);
            }
        });
    }

    /**************************************** private method ****************************************************/
    /**
     * 同步创建明天索引
     *
     * @param physicalId 物理模板id
     * @param retryCount 重试次数
     * @return result
     * @throws ESOperateException
     */
    private boolean syncDeleteTomorrowIndexByPhysicalId(Long physicalId, int retryCount) throws ESOperateException {
        IndexTemplatePhyWithLogic physicalWithLogic = templatePhyService.getTemplateWithLogicById(physicalId);
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

    /**
     * 同步创建今天索引
     * @param physicalId 物理模板id
     * @param retryCount 重试次数
     * @throws ESOperateException
     */
    private boolean syncCreateTodayIndexByPhysicalId(Long physicalId, int retryCount) throws ESOperateException {
        IndexTemplatePhyWithLogic physicalWithLogic = templatePhyService.getTemplateWithLogicById(physicalId);
        if (physicalWithLogic == null || !physicalWithLogic.hasLogic()) {
            return false;
        }
        String todayIndexName = IndexNameFactory.get(physicalWithLogic.getExpression(),
            physicalWithLogic.getLogicTemplate().getDateFormat(), 0, physicalWithLogic.getVersion());
        return esIndexService.syncCreateIndex(physicalWithLogic.getCluster(), todayIndexName, retryCount);
    }

    private boolean syncCreateTomorrowIndexByPhysicalId(Long physicalId, int retryCount) throws ESOperateException {
        IndexTemplatePhyWithLogic physicalWithLogic = templatePhyService.getTemplateWithLogicById(physicalId);
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
}
