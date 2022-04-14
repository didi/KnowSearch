package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESZeusConfigDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.esconfig.EsConfigActionEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;

import java.util.List;

/**
 * @author lyn
 * @date 2020-12-30
 */
public interface ESClusterConfigService {
    /**
     * 获取宙斯ES执行脚本配置内容
     * @param esZeusConfigDTO
     * @param configAction 配置项动作
     * @return
     */
    Result<String> getZeusConfigContent(ESZeusConfigDTO esZeusConfigDTO, Integer configAction);

    /**
     * 根据集群id获取配置
     * @param clusterId
     * @return
     */
    Result<List<ESConfig>> listEsClusterConfigByClusterId(Long clusterId);

    /**
     * 获取集群默认配置Client\master\data节点都一致
     * @param type
     * @return
     */
    ESConfig getEsClusterTemplateConfig(String type);

    /**
     * 获取ES集群配置信息
     * @param clusterId
     * @param type
     * @param engin
     * @return
     */
    ESConfig getByClusterIdAndTypeAndEngin(Long clusterId, String type, String engin);

    /**
     * 根据主键获取ES集群有效配置信息
     * @param id
     * @return
     */
    ESConfig getEsClusterConfigById(Long id);

    /**
     * 根据id获取配置信息(不区分是否有效无效)
     * @param id
     * @return
     */
    ESConfig getEsConfigById(Long id);

    /**
     * 新增/编辑ES集群配置
     * @param param
     * @param operator
     * @param actionEnum ADD EDIT
     * @return
     */
    Result<Long> esClusterConfigAction(ESConfigDTO param, EsConfigActionEnum actionEnum, String operator);

    /**
     * 批量增加Es配置
     * @param esConfigs
     * @param operator
     * @return
     */
    List<Long> batchCreateEsClusterConfigs(List<ESConfigDTO> esConfigs, String operator);

    /**
     * 删除ES集群配置
     * @param configId
     * @param operator
     * @return
     */
    Result<Void> deleteEsClusterConfig(Long configId, String operator);

    /**
     * 设置配置为无效
     * @param id
     * @return
     */
    Result<Void> setConfigValid(Long id);

    /**
     * 设置原始配置为无效
     * @param esConfig
     * @return
     */
    Result<Void> setOldConfigInvalid(ESConfig esConfig);

    /**
     * 修改配置详情
     * @param param
     * @param operator
     * @return
     */
    Result<Void> editConfigDesc(ESConfigDTO param, String operator);

    /**
     * 删除固定集群角色配置类型下的配置信息
     * @param clusterId 物理集群id
     * @param typeName 配置类型
     * @param enginName 角色名称
     * @return
     */
    Result<Void> deleteByClusterIdAndTypeAndEngin(Long clusterId, String typeName, String enginName);
}
