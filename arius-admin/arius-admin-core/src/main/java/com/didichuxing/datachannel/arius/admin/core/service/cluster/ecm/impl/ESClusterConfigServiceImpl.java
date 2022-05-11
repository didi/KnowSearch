package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESZeusConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esconfig.ESConfigPO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESClusterConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESClusterConfigDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.ES_CLUSTER_CONFIG;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.CLUSTER_CONFIG;

/**
 * @author lyn
 * @date 2020-12-30
 */
@Service
public class ESClusterConfigServiceImpl implements ESClusterConfigService {

    private static final ILog    LOGGER                  = LogFactory.getLog(ESClusterConfigServiceImpl.class);

    @Autowired
    private ClusterPhyService esClusterPhyService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private ESClusterConfigDAO   esClusterConfigDAO;

    private static final String  DEFAULT_CONFIG_TEMPLATE = "template";

    private static final Long    SYSTEM_CLUSTER_ID       = 1L;

    private static final Integer DEFAULT_CONFIG_VERSION = 1;

    @Override
    public Result<String> getZeusConfigContent(ESZeusConfigDTO esZeusConfigDTO, Integer configAction) {
        Result<Void> r = preCheck(esZeusConfigDTO);
        if (r.failed()) {
            return Result.buildFrom(r);
        }

        ClusterPhy clusterPhy = esClusterPhyService.getClusterByName(esZeusConfigDTO.getClusterName());
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail("es clusterPhy is empty");
        }

        if (configAction.equals(EsConfigActionEnum.DELETE.getCode())) {
            return Result.buildSucc();
        }

        if (configAction.equals(EDIT.getCode())) {
            ESConfigPO oldESConfigPO = esClusterConfigDAO.getByClusterIdAndTypeAndEngin(clusterPhy.getId().longValue(),
                    esZeusConfigDTO.getTypeName(), esZeusConfigDTO.getEnginName());
            if (AriusObjUtils.isNull(oldESConfigPO) || AriusObjUtils.isNull(oldESConfigPO.getVersionConfig())) {
                return Result.buildFail("old es config is empty");
            }
            ESConfigPO newESConfigPO = esClusterConfigDAO.getByClusterIdAndTypeAndEnginAndVersion(clusterPhy.getId().longValue(),
                    esZeusConfigDTO.getTypeName(), esZeusConfigDTO.getEnginName(), oldESConfigPO.getVersionConfig() + 1);
            if (AriusObjUtils.isNull(newESConfigPO) || AriusObjUtils.isBlack(newESConfigPO.getConfigData())) {
                return Result.buildFail("es config is empty");
            }
            return Result.build(Boolean.TRUE, newESConfigPO.getConfigData());
        }

        if (configAction.equals(ADD.getCode())) {
            ESConfigPO esConfigPO = esClusterConfigDAO.getByClusterIdAndTypeAndEnginAndVersion(clusterPhy.getId().longValue(),
                    esZeusConfigDTO.getTypeName(), esZeusConfigDTO.getEnginName(), DEFAULT_CONFIG_VERSION);
            if (AriusObjUtils.isNull(esConfigPO) || AriusObjUtils.isBlack(esConfigPO.getConfigData())) {
                return Result.buildFail("es config is empty");
            }
            return Result.build(Boolean.TRUE, esConfigPO.getConfigData());
        }
        return Result.buildFail();
    }

    @Override
    public Result<List<ESConfig>> listEsClusterConfigByClusterId(Long clusterId) {
        List<ESConfigPO> esConfigs = esClusterConfigDAO.listByClusterId(clusterId);
        List<ESConfigPO> respEsConfigs = Lists.newArrayList();

        List<ESConfigPO> defaultConfigs = esClusterConfigDAO.listByClusterId(1L);
        if (CollectionUtils.isEmpty(defaultConfigs)) {
            return Result.buildFail("default configs template is empty");
        }

        if (CollectionUtils.isEmpty(esConfigs)) {
            return Result.buildSucc(ConvertUtil.list2List(respEsConfigs, ESConfig.class));
        } else {
            respEsConfigs.addAll(esConfigs);
        }

        return Result.buildSucc(ConvertUtil.list2List(respEsConfigs, ESConfig.class));
    }

    @Override
    public ESConfig getEsClusterTemplateConfig(String type) {
        return ConvertUtil.obj2Obj(
            esClusterConfigDAO.getByClusterIdAndTypeAndEngin(SYSTEM_CLUSTER_ID, type, DEFAULT_CONFIG_TEMPLATE),
            ESConfig.class);
    }

    @Override
    public ESConfig getByClusterIdAndTypeAndEngin(Long clusterId, String type, String engin) {
        return ConvertUtil.obj2Obj(esClusterConfigDAO.getByClusterIdAndTypeAndEngin(clusterId, type, engin),
            ESConfig.class);
    }

    @Override
    public ESConfig getEsClusterConfigById(Long id) {
        return ConvertUtil.obj2Obj(esClusterConfigDAO.getValidEsConfigById(id), ESConfig.class);
    }

    @Override
    public ESConfig getEsConfigById(Long id) {
        return ConvertUtil.obj2Obj(esClusterConfigDAO.getById(id), ESConfig.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> esClusterConfigAction(ESConfigDTO param, EsConfigActionEnum actionEnum, String operator) {
        Result<Void> r = checkParam(param, operator, actionEnum);
        if (r.failed()) {
            return Result.buildFrom(r);
        }
        param.setVersionConfig(DEFAULT_CONFIG_VERSION);
        if (EDIT.getCode() == actionEnum.getCode()) {
            ESConfig esConfig = getEsClusterConfigById(param.getId());
            param.setVersionConfig(esConfig.getVersionConfig() + 1);
        }
        ESConfigPO esConfigPo = ConvertUtil.obj2Obj(param, ESConfigPO.class);
        boolean success = (1 == esClusterConfigDAO.insertSelective(esConfigPo));
        if (success) {
            operateRecordService.save(ES_CLUSTER_CONFIG, CLUSTER_CONFIG, param.getId(), "", operator);
            return Result.buildSucc(esConfigPo.getId());
        }

        return Result.buildFail();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> batchCreateEsClusterConfigs(List<ESConfigDTO> esConfigs, String operator) {
        List<Long> clusterConfigIds = Lists.newArrayList();
        esConfigs.stream().filter(Objects::nonNull).forEach(config -> {
            Result<Long> r = esClusterConfigAction(config, ADD, operator);
            if (r.failed()) {
                LOGGER.error("class=ESClusterConfigServiceImpl||method=batchCreateEsClusterConfigs||"
                             + "clusterId={}||typeName={}||enginName={}||msg=failed to create the es cluster config",
                    config.getClusterId(), config.getTypeName(), config.getEnginName());
                return;
            }

            clusterConfigIds.add(r.getData());
        });

        return clusterConfigIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteEsClusterConfig(Long configId, String operator) {
        boolean success = (1 == esClusterConfigDAO.delete(configId));
        if (success) {
            operateRecordService.save(ES_CLUSTER_CONFIG, OperationEnum.DELETE, configId, "", operator);
            return Result.buildSucc();
        }

        return Result.buildFail();
    }

    @Override
    public Result<Void> setConfigValid(Long id) {
        boolean success = 1 == esClusterConfigDAO.updateConfigValidById(id);
        if (success) {
            return Result.buildSucc();
        }
        return Result.buildFail();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> editConfigDesc(ESConfigDTO param, String operator) {
        Result<Void> result = checkEditConfigValid(param);
        if (result.failed()) {
            return result;
        }

        try {
            boolean success = 1 == esClusterConfigDAO.update(ConvertUtil.obj2Obj(param, ESConfigPO.class));
            if (success) {
                operateRecordService.save(ES_CLUSTER_CONFIG, CLUSTER_CONFIG, param.getId(), "", operator);
                return Result.buildSucc();
            }

            return Result.buildFail();
        } catch (Exception e) {
            LOGGER.error("class=ESClusterConfigServiceImpl||method=editConfigDesc||" + "configId={}||msg={}",
                param.getId(), e.getStackTrace());
        }
        return Result.buildFail();
    }

    @Override
    public Result<Void> deleteByClusterIdAndTypeAndEngin(Long clusterId, String typeName, String enginName) {
        try {
            esClusterConfigDAO.deleteByClusterIdAndTypeAndEngin(clusterId, typeName, enginName);
            return Result.buildSuccWithMsg("集群角色下对应类型的配置已删除");
        } catch (Exception e) {
            LOGGER.error("class=ESClusterConfigServiceImpl||method=deleteByClusterIdAndTypeAndEngin||msg={}", e.getStackTrace());
        }
        return Result.buildFail();
    }

    @Override
    public Result<Void> setOldConfigInvalid(ESConfig esConfig) {
        int oldVersion = esConfig.getVersionConfig() - 1;
        ESConfigPO esConfigPo = esClusterConfigDAO.getByClusterIdAndTypeAndEnginAndVersion(esConfig.getClusterId(),
            esConfig.getTypeName(), esConfig.getEnginName(), oldVersion);
        if (AriusObjUtils.isNull(esConfigPo)) {
            return Result.buildFail("the old config is empty");
        }

        esConfigPo.setSelected(0);
        boolean success = 1 == esClusterConfigDAO.update(esConfigPo);
        if (success) {
            return Result.buildSucc();
        }
        return Result.buildFail();
    }

    /*************************************************private**********************************************************/
    private Result<Void> preCheck(ESZeusConfigDTO esZeusConfigDTO) {
        if (AriusObjUtils.isBlack(esZeusConfigDTO.getClusterName())) {
            return Result.buildFail("cluster name is empty");
        }

        if (AriusObjUtils.isBlack(esZeusConfigDTO.getEnginName())) {
            return Result.buildFail("engin name is empty");
        }

        if (AriusObjUtils.isBlack(esZeusConfigDTO.getTypeName())) {
            return Result.buildFail("type name is empty");
        }

        return Result.buildSucc();
    }

    private Result<Void> checkParam(ESConfigDTO param, String operator, EsConfigActionEnum actionEnum) {
        if (ADD.getCode() == actionEnum.getCode()) {
            Result<Void> esConfigDTOIsEmpty = isEmpty(param, operator);
            if (esConfigDTOIsEmpty.failed()) {
                return esConfigDTOIsEmpty;
            }

            if (!AriusObjUtils.isNull(esClusterConfigDAO.getByClusterIdAndTypeAndEngin(param.getClusterId(),
                param.getTypeName(), param.getEnginName()))) {
                return Result.buildFail("config is exist, forbid repeat create config");
            }

        } else if (EDIT.getCode() == actionEnum.getCode()) {
            if (AriusObjUtils.isNull(param.getId())) {
                return Result.buildParamIllegal("id is empty");
            }

            if (AriusObjUtils.isNull(esClusterConfigDAO.getValidEsConfigById(param.getId()))) {
                return Result.buildParamIllegal("config is not exist, please create first");
            }

            if (AriusObjUtils.isNull(getEsClusterConfigById(param.getId()))) {
                return Result.buildFail("es cluster config is empty");
            }
        }

        return Result.buildSucc();
    }

    private Result<Void> isEmpty(ESConfigDTO param, String operator) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("esConfigDTO is empty");
        }

        if (AriusObjUtils.isBlack(operator)) {
            return Result.buildParamIllegal("operator is empty");
        }

        if (AriusObjUtils.isBlack(param.getConfigData())) {
            return Result.buildParamIllegal("config data is empty");
        }

        if (AriusObjUtils.isBlack(param.getEnginName())) {
            return Result.buildParamIllegal("engin name is empty");
        }

        if (AriusObjUtils.isBlack(param.getTypeName())) {
            return Result.buildParamIllegal("type name is empty");
        }

        if (AriusObjUtils.isNull(param.getClusterId())) {
            return Result.buildParamIllegal("clusterId name is empty");
        }
        return Result.buildSucc();
    }

    private Result<Void> checkEditConfigValid(ESConfigDTO param) {
        if (AriusObjUtils.isNull(param.getId())) {
            return Result.buildParamIllegal("集群配置Id为空");
        }

        ESConfigPO esConfig = esClusterConfigDAO.getById(param.getId());
        if (AriusObjUtils.isNull(esConfig)) {
            return Result.buildParamIllegal("集群配置不存在");
        }

        if (AriusObjUtils.isBlack(esConfig.getConfigData())) {
            return Result.buildParamIllegal("集群配置内容为空");
        }

        if (esConfig.getConfigData().equals(param.getConfigData())) {
            return Result.buildParamIllegal("不允许修改集群配置数据信息");
        }

        return Result.buildSucc();
    }
}
