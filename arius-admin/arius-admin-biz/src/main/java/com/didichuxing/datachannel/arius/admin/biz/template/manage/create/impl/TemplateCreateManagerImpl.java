package com.didichuxing.datachannel.arius.admin.biz.template.manage.create.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.template.manage.create.TemplateCreateManager;
import com.didichuxing.datachannel.arius.admin.biz.template.manage.mapping.TemplateMappingManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateWithCreateInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.event.template.TemplateCreateEvent;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusTypeProperty;
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

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.DEFAULT_INDEX_MAPPING_TYPE;
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
    private TemplateLogicManager templateLogicManager;

    @Autowired
    private TemplateMappingManager templateMappingManager;

    @Override
    public Result<Void> create(IndexTemplateWithCreateInfoDTO param, String operator, Integer appId) {
        Result<Void> validParamResult = validateParam(param, appId);
        if (validParamResult.failed()) {
            return validParamResult;
        }

        IndexTemplateDTO indexTemplateDTO = buildTemplateDTO(param, appId);
        Result<Void> validTemplateResult = indexTemplateService.validateTemplate(buildTemplateDTO(param, appId), ADD);
        if (validTemplateResult.failed()) {
            return validTemplateResult;
        }

        try {
            Result<Integer> createResult = templateLogicManager.createLogicTemplate(indexTemplateDTO, operator);
            if (createResult.success()) {
                SpringTool.publish(new TemplateCreateEvent(this, indexTemplateDTO));
            }
        } catch (Exception e) {
            LOGGER.error("class=TemplateCreateManager||method=create||msg=create template failed", e);
            return Result.buildFail();
        }

        return Result.buildSucc();
    }


    private Result<Void> validateParam(IndexTemplateWithCreateInfoDTO param, Integer appId) {
        if (AriusObjUtils.isNull(param.getResponsible())) {
            return Result.buildParamIllegal("责任人为空");
        }

        if (AriusObjUtils.isNull(param.getCyclicalRoll())) {
            return Result.buildParamIllegal("索引分区设置为空");
        }

        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(param.getResourceId());
        if (clusterLogic == null) {
            return Result.buildParamIllegal("集群不存在");
        }

        if (param.getCyclicalRoll() && AriusObjUtils.isNull(param.getDateField())) {
            return Result.buildParamIllegal("分区字段为空");
        }

        if (param.getMapping() != null) {
            Result<Void> validMappingResult = templateMappingManager.validMapping(param.getMapping());
            if (validMappingResult.failed()) {
                return validMappingResult;
            }
        }

        if (!param.getDataCenter().equals(clusterLogic.getDataCenter())) {
            return Result.buildParamIllegal("集群数据中心不符");
        }

        return Result.buildSucc();
    }

    private IndexTemplateDTO buildTemplateDTO(IndexTemplateWithCreateInfoDTO param, Integer appId) {
        IndexTemplateDTO indexTemplateDTO = ConvertUtil.obj2Obj(param, IndexTemplateDTO.class);

        indexTemplateDTO.setAppId(appId);
        //todo: 移除quota 后删掉这行
        indexTemplateDTO.setQuota(param.getDiskSize());

        buildCyclicalRoll(indexTemplateDTO, param);
        buildShardNum(indexTemplateDTO, param);
        buildPhysicalInfo(indexTemplateDTO, param);

        return indexTemplateDTO;
    }

    private void buildCyclicalRoll(IndexTemplateDTO indexTemplateDTO, IndexTemplateWithCreateInfoDTO param) {
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
                double incrementPerDay = param.getDiskSize() / param.getExpireTime();
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

    private void buildPhysicalInfo(IndexTemplateDTO indexTemplateDTO, IndexTemplateWithCreateInfoDTO param) {
        IndexTemplatePhyDTO indexTemplatePhyDTO = ConvertUtil.obj2Obj(indexTemplateDTO, IndexTemplatePhyDTO.class);

        indexTemplatePhyDTO.setLogicId(NOT_CHECK);
        indexTemplatePhyDTO.setGroupId(UUID.randomUUID().toString());
        indexTemplatePhyDTO.setRole(TemplateDeployRoleEnum.MASTER.getCode());
        indexTemplatePhyDTO.setShard(indexTemplateDTO.getShardNum());
        indexTemplatePhyDTO.setDefaultWriterFlags(true);

        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(param.getResourceId());
        indexTemplatePhyDTO.setCluster(clusterRegion.getPhyClusterName());
        if (null == indexTemplateDTO.getRegionId()) {
            indexTemplateDTO.setRegionId(clusterRegion.getId().intValue());
        }


        //todo: set setting here
        if (StringUtils.isNotBlank(param.getSettings())) {
            indexTemplatePhyDTO.setSettings(new AriusIndexTemplateSetting());
        }

        //todo: set mapping here
        if (StringUtils.isNotBlank(param.getMapping())) {
            AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
            ariusTypeProperty.setTypeName(DEFAULT_INDEX_MAPPING_TYPE);
            if (StringUtils.isBlank(param.getMapping())) {
                param.setMapping("{}");
            }
            ariusTypeProperty.setProperties(JSON.parseObject(param.getMapping()));
            // 这里都是设置默认的type类型的类型名称
            indexTemplatePhyDTO.setMappings(ariusTypeProperty.toMappingJSON().getJSONObject(DEFAULT_INDEX_MAPPING_TYPE).toJSONString());
        }

        indexTemplateDTO.setPhysicalInfos(Lists.newArrayList(indexTemplatePhyDTO));
    }

    private void buildShardNum(IndexTemplateDTO indexTemplateDTO, IndexTemplateWithCreateInfoDTO param) {
        if (param.getCyclicalRoll()) {
            int expireTime = param.getExpireTime();
            if (expireTime < 0) {
                // 如果数据永不过期，平台会按着180天来计算每日数据增量，最终用于生成模板shard
                expireTime = 180;
            }

            if (TemplateUtils.isSaveByDay(indexTemplateDTO.getDateFormat())) {
                // 按天滚动
                indexTemplateDTO.setShardNum(genShardNumBySize(param.getDiskSize() / expireTime));
            } else {
                // 按月滚动
                indexTemplateDTO.setShardNum(genShardNumBySize((param.getDiskSize() / expireTime) * 30));
            }
        } else {
            indexTemplateDTO.setShardNum(genShardNumBySize(param.getDiskSize()));
        }
    }

    private Integer genShardNumBySize(Double size) {
        double shardNumCeil = Math.ceil(size / G_PER_SHARD);
        return (int) shardNumCeil;
    }

}
