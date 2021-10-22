package com.didichuxing.datachannel.arius.admin.core.service.template.physic;

import java.util.Collection;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplatePhysicalPO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

/**
 * @author d06679
 * @date 2019/3/29
 */
public interface TemplatePhyService {
    /**
     * 条件查询
     * @param param 参数
     * @return 物理模板列表
     */
    List<IndexTemplatePhy> getByCondt(IndexTemplatePhysicalDTO param);

    /**
     * 查询指定逻辑模板对应的物理模板
     * @param logicId 逻辑模板
     * @return list
     */
    List<IndexTemplatePhy> getTemplateByLogicId(Integer logicId);

    /**
     * 从缓存中查询指定逻辑模板对应的物理模板
     * @param logicId 逻辑模板
     * @return list
     */
    List<IndexTemplatePhy> getTemplatesByLogicIdFromCache(Integer logicId);

    /**
     * 查询指定id的模板
     * @param physicalId 物理模板id
     * @return result
     */
    IndexTemplatePhy getTemplateById(Long physicalId);

    /**
     * 查询指定id的模板 包含逻辑模板信息
     * @param physicalId 物理模板id
     * @return result
     */
    IndexTemplatePhyWithLogic getTemplateWithLogicById(Long physicalId);

    /**
     * 删除
     * @param physicalId 物理模板id
     * @param operator 操作人
     * @return result
     * @throws ESOperateException e
     */
    Result delTemplate(Long physicalId, String operator) throws ESOperateException;

    /**
     * 删除
     * @param logicId id
     * @param operator 操作人
     * @return result
     * @throws ESOperateException e
     */
    Result delTemplateByLogicId(Integer logicId, String operator) throws ESOperateException;

    /**
     * 修改由于逻辑模板修改而物理模板需要同步修改的属性
     * @param param 参数
     * @param operator 操作人
     * @return result
     */
    Result editTemplateFromLogic(IndexTemplateLogicDTO param, String operator) throws ESOperateException;

    /**
     * 通过集群和名字查询
     * @param cluster 集群
     * @param templateName 名字
     * @return result 不存在返回null
     */
    IndexTemplatePhy getTemplateByClusterAndName(String cluster, String templateName);

    /**
     * 通过集群和名字查询
     * @param cluster 集群
     * @param templateName 名字
     * @return result 不存在返回null
     */
    IndexTemplatePhyWithLogic getTemplateWithLogicByClusterAndName(String cluster, String templateName);

    /**
     * 根据物理魔板状态获取模板列表
     * @param cluster 集群
     * @param status 状态 1 常规    -1 删除中     -2 已删除
     * @return list
     */
    List<IndexTemplatePhy> getTemplateByClusterAndStatus(String cluster, int status);

    /**
     * 根据物理魔板状态获取模板列表
     * @param cluster 集群
     * @return list
     */
    List<IndexTemplatePhy> getNormalTemplateByCluster(String cluster);

    /**
     * 根据物理魔板状态获取模板列表
     * @param cluster 集群
     * @param racks racks
     * @return list
     */
    List<IndexTemplatePhy> getNormalTemplateByClusterAndRack(String cluster, Collection<String> racks);

    /**
     * 获取模板匹配的索引列表，按着时间排序
     * @param physicalId 物理模板id
     * @return list
     */
    List<String> getMatchNoVersionIndexNames(Long physicalId);

    /**
     * 获取模板匹配的索引列表，按着时间排序
     * @param physicalId 物理模板id
     * @return list
     */
    List<String> getMatchIndexNames(Long physicalId);

    /**
     * 批量获取模板信息
     * @param physicalIds 物理模板id
     * @return list
     */
    List<IndexTemplatePhyWithLogic> getTemplateWithLogicByIds(List<Long> physicalIds);

    /**
     * 根据名字查询
     * @param template 名字
     * @return list
     */
    List<IndexTemplatePhyWithLogic> getTemplateWithLogicByName(String template);

    /**
     * 获取全量
     * @return list
     */
    List<IndexTemplatePhy> listTemplate();

    /**
     * 从缓存中获取全量
     * @return
     */
    List<IndexTemplatePhy> listTemplateWithCache();

    /**
     * 获取缓存中获取全量
     * @return
     */
    List<IndexTemplatePhy> listTemplateByOpenCache();

    /**
     * 获取IndexTemplatePhysicalWithLogic
     * @return list
     */
    List<IndexTemplatePhyWithLogic> listTemplateWithLogic();

    /**
     * listTemplateWithLogicWithCache
     * @return
     */
    List<IndexTemplatePhyWithLogic> listTemplateWithLogicWithCache();

    /**
     * 根绝逻辑模板id列表查询
     * @param logicIds 列表
     * @return list
     */
    List<IndexTemplatePhy> getTemplateByLogicIds(List<Integer> logicIds);

    /**
     * 修改物理模板，名称
     * @param physical 模板
     * @param operator 操作人
     * @return
     */
    Result updateTemplateName(IndexTemplatePhy physical, String operator) throws ESOperateException;

    IndexTemplatePhyWithLogic buildIndexTemplatePhysicalWithLogic(TemplatePhysicalPO physicalPO);

    /**
     * 获取regionId下的物理模板
     */
    List<IndexTemplatePhy> getTemplateByRegionId(Long regionId);

}
