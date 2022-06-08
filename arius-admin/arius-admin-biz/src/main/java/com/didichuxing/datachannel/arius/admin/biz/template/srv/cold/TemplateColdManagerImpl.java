package com.didichuxing.datachannel.arius.admin.biz.template.srv.cold;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.MILLIS_PER_DAY;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.BATCH_CHANGE_TEMPLATE_HOT_DAYS;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_COLD;
import static com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory.genIndexNameClear;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateDAO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * 索引冷存服务
 * @author zqr
 * @date 2020-09-09
 */
@Service
public class TemplateColdManagerImpl extends BaseTemplateSrv implements TemplateColdManager {

    public static final int MAX_HOT_DAY = 2;
    public static final int MIN_HOT_DAY = -2;
    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @Autowired
    private ESClusterService       esClusterService;

    @Autowired
    private ESIndexService         esIndexService;

    @Autowired
    private IndexTemplateDAO indexTemplateDAO;

    @Autowired
    private ClusterPhyService      clusterPhyService;

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_COLD;
    }

    /**
     * 获取cold索引
     *
     * @param physicalId 物理模板ID
     * @return set集合 v1:冷节点索引列表 v2：热节点索引列表
     */
    @Override
    public Tuple</*冷节点索引列表*/Set<String>, /*热节点索引列表*/Set<String>> getColdAndHotIndex(Long physicalId) {
        IndexTemplatePhyWithLogic templatePhysicalWithLogic = indexTemplatePhyService
            .getTemplateWithLogicById(physicalId);
        if (templatePhysicalWithLogic == null) {
            return new Tuple<>();
        }

        int hotTime = templatePhysicalWithLogic.getLogicTemplate().getHotTime();

        if (hotTime <= 0) {
            LOGGER.info("class=TemplateColdManagerImpl||method=getColdIndex||template={}||msg=hotTime illegal", templatePhysicalWithLogic.getName());
            return new Tuple<>();
        }

        if (hotTime >= templatePhysicalWithLogic.getLogicTemplate().getExpireTime()) {
            LOGGER.info("class=TemplateColdManagerImpl||method=getColdIndex||||template={}||msg=all index is hot",
                templatePhysicalWithLogic.getName());
            return new Tuple<>();
        }

        return templatePhyManager.getHotAndColdIndexByBeforeDay(templatePhysicalWithLogic, hotTime);
    }

    /**
     * 根据接入集群可以连接的地址校验是否可以开启冷热分离服务
     * @param httpAddresses client地址
     * @return 校验的结果，返回模板服务id
     */
    @Override
    public Result<Boolean> checkOpenTemplateSrvWhenClusterJoin(String httpAddresses, String password) {
        return Result.buildSucc();
    }

    /**
     * 校验指定的已经入库的物理集群是否可以开启冷热分离
     * @param phyCluster 物理集群名称
     * @return 校验结果
     */
    @Override
    public Result<Boolean> checkOpenTemplateSrvByCluster(String phyCluster) {
        /*if (StringUtils.isBlank(phyCluster)) {
            return Result.buildFail("物理集群名称为空");
        }

        //指定物理集群未设置冷节点
        List<String> coldRackList = Lists.newArrayList(clusterPhyService.listColdRacks(phyCluster));
        if (CollectionUtils.isEmpty(coldRackList)) {
            LOGGER.warn("class=TemplateColdManagerImpl||method=move2ColdNode||cluster={}||no cold rack", phyCluster);
            return Result.buildFail("接入集群不具备开启【冷热分离索引模板服务】条件，需要满足【集群版本6.6.1以上；具备cold属性节点】");
        }*/

        return Result.buildSucc();
    }

    /**
     * 确保搬迁配置是打开的
     *
     * 修改索引的rack
     *
     * 通过tts任务触发，任务需要幂等，需要多次重试，确保成功
     *
     * @return result
     */
    @Override
    public Result<Boolean> move2ColdNode(String phyCluster) {
        /*if (!isTemplateSrvOpen(phyCluster)) {
            return Result.buildFail(phyCluster + " 没有开启冷存搬迁服务");

        }

        List<String> coldRackList = Lists.newArrayList(clusterPhyService.listColdRacks(phyCluster));

        if (CollectionUtils.isEmpty(coldRackList)) {
            LOGGER.warn("class=TemplateColdManagerImpl||method=move2ColdNode||cluster={}||no cold rack", phyCluster);
            return Result.buildFail(phyCluster + "没有冷节点");
        }

        coldRackList.sort(RackUtils::compareByName);
        String coldRack = String.join(",", coldRackList);

        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getNormalTemplateByCluster(phyCluster);

        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return Result.buildSucc(true);
        }

        LOGGER.info("class=TemplateColdManagerImpl||method=move2ColdNode||cluster={}||coldRacks={}", phyCluster, coldRack);

        int succ = 0;
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            try {
                Result<Boolean> moveResult = movePerTemplate(templatePhysical, coldRack);
                if (moveResult.success()) {
                    succ++;
                } else {
                    LOGGER.warn("class=TemplateColdManagerImpl||method=move2ColdNode||template={}||msg=move2ColdNode fail",
                        templatePhysical.getName());
                }
            } catch (Exception e) {
                LOGGER.warn("class=TemplateColdManagerImpl||method=move2ColdNode||template={}||errMsg={}", templatePhysical.getName(), e.getMessage(),
                    e);
            }
        }

        return Result.buildSucc(succ * 1.0 / templatePhysicals.size() > 0.8);*/
        return Result.buildSucc();
    }

    /**
     * 获取当前集群默认hotDay
     *
     * @param phyCluster 当前集群名称
     * @return
     */
    @Override
    public int fetchClusterDefaultHotDay(String phyCluster) {
        int hotDay = -1;
        Set<String> enableClusterSet = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
            "platform.govern.cold.data.move2ColdNode.enable.clusters", "", ",");
        if (enableClusterSet.contains(phyCluster)) {
            int defaultHotDay = getDefaultHotDay();
            if (defaultHotDay > 0) {
                hotDay = defaultHotDay;
            }
        }

        LOGGER.info("class=TemplateColdManagerImpl||method=fetchClusterDefaultHotDay||msg=no changed||cluster={}||enableClusters={}||version={}",
            phyCluster, JSON.toJSONString(enableClusterSet), hotDay);

        return hotDay;
    }

    /**
     * 批量修改hotDays
     *
     * @param days     变量
     * @param operator 操作人
     * @return result
     */
    @Override
    public Result<Integer> batchChangeHotDay(Integer days, String operator) {
        if (days > MAX_HOT_DAY || days < MIN_HOT_DAY) {
            return Result.buildParamIllegal("days参数非法, [-2, 2]");
        }

        int count = indexTemplateDAO.batchChangeHotDay(days);

        LOGGER.info("class=TemplateColdManagerImpl||method=batchChangeHotDay||days={}||count={}||operator={}", days, count, operator);

        operateRecordService.save(ModuleEnum.PLATFORM_OP, BATCH_CHANGE_TEMPLATE_HOT_DAYS, -1,
            "deltaHotDays:" + days + ";editCount:" + count, operator);

        return Result.buildSucc(count);
    }

    /**************************************************** private method ****************************************************/
    private List<String> getExpList(IndexTemplatePhyWithLogic physicalWithLogic, List<String> indices, int hotDay) {
        List<String> expList = Lists.newArrayList();

        if (hotDay < 0) {
            expList.addAll(indices);
        } else if (TemplateUtils.isOnly1Index(physicalWithLogic.getExpression())) {
            expList.add(physicalWithLogic.getExpression());
        } else {
            if (CollectionUtils.isEmpty(indices)) {
                LOGGER.info("class=TemplateColdManagerImpl||method=updateHotIndexRack||template={}||msg=no matched indices",
                        physicalWithLogic.getName());
                return Lists.newArrayList();
            }
            Set<String> hotIndexNames = Sets.newHashSet();
            for (String indexName : indices) {
                Date indexTime = IndexNameFactory.genIndexTimeByIndexName(
                        genIndexNameClear(indexName, physicalWithLogic.getExpression()), physicalWithLogic.getExpression(),
                        physicalWithLogic.getLogicTemplate().getDateFormat());
                if (indexTime == null) {
                    LOGGER.warn("class=TemplateColdManagerImpl||method=updateHotIndexRack||template={}||msg=parse index time fail",
                            physicalWithLogic.getName());
                    continue;
                }
                long timeIntervalDay = (System.currentTimeMillis() - indexTime.getTime()) / MILLIS_PER_DAY;
                if (timeIntervalDay >= hotDay) {
                    LOGGER.info("class=TemplateColdManagerImpl||method=updateHotIndexRack||template={}||indexName={}||msg=index is cold",
                            physicalWithLogic.getName(), indexName);
                    continue;
                }
                hotIndexNames.add(indexName);
            }
            expList.addAll(hotIndexNames);
        }

        return expList;
    }

    private Result<Boolean> movePerTemplate(IndexTemplatePhy templatePhysical, String coldRacks) throws ESOperateException {
        //获取冷存数据索引列表
        Set<String> coldIndexNames = getColdAndHotIndex(templatePhysical.getId()).getV1();
        //获取热存数据索引列表
        Set<String> hotIndexNames = getColdAndHotIndex(templatePhysical.getId()).getV2();

        //用户修改模板设置更大热数据保存天数，需要将原本迁移到冷存节点上的索引还原到热存节点上
        return Result.buildBoolen(movePerIndexTemplate(templatePhysical, coldRacks, coldIndexNames)
                && movePerIndexTemplate(templatePhysical, templatePhysical.getRack(), hotIndexNames));
    }

    private boolean movePerIndexTemplate(IndexTemplatePhy templatePhysical,
                                         String racks, Set<String> indexNames) throws ESOperateException {
        /*if (CollectionUtils.isEmpty(indexNames)) {
            LOGGER.info("class=TemplateColdManagerImpl||method=movePerIndexTemplate||template={}||msg=no need index", templatePhysical.getName());
            return false;
        } else {
            //冷热节点数据间的迁移
            return esIndexService.syncBatchUpdateRack(templatePhysical.getCluster(), Lists.newArrayList(indexNames), racks, 3);
        }*/
        return false;
    }

    /**
     * 获取配置默认hotDay值
     *
     * @return
     */
    private int getDefaultHotDay() {
        String defaultDay = ariusConfigInfoService.stringSetting(ARIUS_TEMPLATE_COLD, INDEX_TEMPLATE_COLD_DAY_DEFAULT, "");
        LOGGER.info("class=TemplateColdManagerImpl||method=getDefaultHotDay||msg=defaultDay: {}", defaultDay);
        if (StringUtils.isNotBlank(defaultDay)) {
            try {
                JSONObject object = JSON.parseObject(defaultDay);
                return object.getInteger("defaultHotDay");
            } catch (JSONException e) {
                LOGGER.warn("class=TemplateColdManagerImpl||method=getDefaultHotDay||errMsg={}", e.getMessage());
            }
        }
        return -1;
    }
}