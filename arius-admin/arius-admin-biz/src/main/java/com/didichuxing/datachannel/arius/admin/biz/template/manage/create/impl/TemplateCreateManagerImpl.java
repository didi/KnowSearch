package com.didichuxing.datachannel.arius.admin.biz.template.manage.create.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplateAction;
import com.didichuxing.datachannel.arius.admin.biz.template.manage.create.TemplateCreateManager;
import com.didichuxing.datachannel.arius.admin.biz.template.manage.mapping.MappingManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateCreateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.event.template.TemplateCreateEvent;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.G_PER_SHARD;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.core.service.template.physic.impl.IndexTemplatePhyServiceImpl.NOT_CHECK;

/**
 * @author chengxiang
 * @date 2022/5/27
 */
@Service
public class TemplateCreateManagerImpl implements TemplateCreateManager {

    private static final ILog LOGGER = LogFactory.getLog(TemplateCreateManager.class);

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private ClusterRegionService clusterRegionService;

    @Autowired
    private MappingManager mappingManager;

    @Autowired
    private TemplateAction templateAction;

    @Override
    public Result<Void> create(TemplateCreateDTO param, String operator, Integer appId) {
        Result<Void> validParamResult = validateParam(param);
        if (validParamResult.failed()) {
            return validParamResult;
        }

        IndexTemplateDTO indexTemplateDTO = buildTemplateDTO(param, appId);
        try {
            Result<Integer> createResult = templateAction.createWithAutoDistributeResource(indexTemplateDTO, operator);
            if (createResult.success()) {
                SpringTool.publish(new TemplateCreateEvent(this, indexTemplateDTO));
            }
        } catch (Exception e) {
            LOGGER.error("class=TemplateCreateManager||method=create||msg=create template failed", e);
            return Result.buildFail();
        }

        return Result.buildSucc();
    }


    private Result<Void> validateParam(TemplateCreateDTO param) {
        if (AriusObjUtils.isNull(param.getResponsible())) {
            return Result.buildParamIllegal("责任人为空");
        }

        if (AriusObjUtils.isNull(param.getCyclicalRoll())) {
            return Result.buildParamIllegal("索引分区设置为空");
        }

        if (AriusObjUtils.isNull(param.getDiskQuota())) {
            return Result.buildParamIllegal("索引数据总量为空");
        }

        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(param.getResourceId());
        if (clusterLogic == null) {
            return Result.buildParamIllegal("集群不存在");
        }

        if (param.getCyclicalRoll() && AriusObjUtils.isNull(param.getDateField())) {
            return Result.buildParamIllegal("分区字段为空");
        }

        Result<Void> validTemplateResult = indexTemplateService.validateTemplate(ConvertUtil.obj2Obj(param, IndexTemplateDTO.class), ADD);
        if (validTemplateResult.failed()) {
            return validTemplateResult;
        }

        if (param.getMapping() != null) {
            Result<Void> validMappingResult = mappingManager.validMapping(param.getMapping());
            if (validMappingResult.failed()) {
                return validMappingResult;
            }
        }

        if (!param.getDataCenter().equals(clusterLogic.getDataCenter())) {
            return Result.buildParamIllegal("集群数据中心不符");
        }

        return Result.buildSucc();
    }

    private IndexTemplateDTO buildTemplateDTO(TemplateCreateDTO param, Integer appId) {
        IndexTemplateDTO indexTemplateDTO = ConvertUtil.obj2Obj(param, IndexTemplateDTO.class);

        indexTemplateDTO.setAppId(appId);

        buildCyclicalRoll(indexTemplateDTO, param);
        buildShardNum(indexTemplateDTO, param);
        buildPhysicalInfo(indexTemplateDTO, param);

        return indexTemplateDTO;
    }

    private void buildCyclicalRoll(IndexTemplateDTO indexTemplateDTO, TemplateCreateDTO param) {
        if (!param.getCyclicalRoll()) {
            indexTemplateDTO.setExpression(param.getName());
            indexTemplateDTO.setDateFormat("");
            indexTemplateDTO.setExpireTime(-1);
            indexTemplateDTO.setDateField("");
        } else {
            indexTemplateDTO.setExpression(param.getName() + "*");
            // 数据不会过期，必须按月滚动
            if (param.getExpireTime() < 0) {
                indexTemplateDTO.setDateFormat(AdminConstant.YY_MM_DATE_FORMAT);
            } else {
                //每天的数据增量大于200G或者保存时长小于30天 按天存储
                double incrementPerDay = param.getDiskQuota() / param.getExpireTime();
                if (incrementPerDay >= 200.0 || param.getExpireTime() <= 30) {
                    if (StringUtils.isNotBlank(param.getDateField()) && !AdminConstant.MM_DD_DATE_FORMAT.equals(param.getDateField())) {
                        indexTemplateDTO.setDateFormat(AdminConstant.YY_MM_DD_DATE_FORMAT);
                    }
                } else {
                    indexTemplateDTO.setDateFormat(AdminConstant.YY_MM_DATE_FORMAT);
                }
            }
        }
    }

    private void buildPhysicalInfo(IndexTemplateDTO indexTemplateDTO, TemplateCreateDTO param) {
        IndexTemplatePhyDTO indexTemplatePhyDTO = ConvertUtil.obj2Obj(indexTemplateDTO, IndexTemplatePhyDTO.class);

        indexTemplatePhyDTO.setLogicId(NOT_CHECK);
        indexTemplatePhyDTO.setGroupId(UUID.randomUUID().toString());
        indexTemplatePhyDTO.setRole(TemplateDeployRoleEnum.MASTER.getCode());
        indexTemplatePhyDTO.setShard(indexTemplateDTO.getShardNum());

        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(param.getResourceId());
        indexTemplatePhyDTO.setCluster(clusterRegion.getPhyClusterName());


        //todo: set setting here
        //todo: set mapping here

        indexTemplateDTO.setPhysicalInfos(Lists.newArrayList(indexTemplatePhyDTO));
    }

    private void buildShardNum(IndexTemplateDTO indexTemplateDTO, TemplateCreateDTO param) {
        if (param.getCyclicalRoll()) {
            int expireTime = param.getExpireTime();
            if (expireTime < 0) {
                // 如果数据永不过期，平台会按着180天来计算每日数据增量，最终用于生成模板shard
                expireTime = 180;
            }

            if (TemplateUtils.isSaveByDay(indexTemplateDTO.getDateFormat())) {
                // 按天滚动
                indexTemplateDTO.setShardNum(genShardNumBySize(param.getDiskQuota() / expireTime));
            } else {
                // 按月滚动
                indexTemplateDTO.setShardNum(genShardNumBySize((param.getDiskQuota() / expireTime) * 30));
            }
        } else {
            indexTemplateDTO.setShardNum(genShardNumBySize(param.getDiskQuota()));
        }
    }

    private Integer genShardNumBySize(Double size) {
        double shardNumCeil = Math.ceil(size / G_PER_SHARD);
        return (int) shardNumCeil;
    }

}
