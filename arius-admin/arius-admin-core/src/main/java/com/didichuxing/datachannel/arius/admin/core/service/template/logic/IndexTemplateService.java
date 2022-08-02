package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateRateLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author d06679
 * @date 2019/3/29
 */
public interface IndexTemplateService {
    /**
     * 条件查询
     * @param param 条件
     * @return 逻辑模板列表
     */
    List<IndexTemplate> listLogicTemplates(IndexTemplateDTO param);

    /**
     * 模糊查询
     * @param param 模糊查询条件
     * @return      List<IndexTemplateLogic>
     */
    List<IndexTemplate> fuzzyLogicTemplatesByCondition(IndexTemplateDTO param);

    /**
     * 模糊分页查询模板列表信息
     */
    List<IndexTemplate> pagingGetLogicTemplatesByCondition(TemplateConditionDTO param);

    /**
     * 模糊分页查询「模板服务」列表信息
     * @param param 模糊查询条件
     * @return
     */
    List<IndexTemplate> pagingGetTemplateSrvByCondition(TemplateQueryDTO param);

    /**
     * 模糊查询统计总命中数, 用于前端分页
     * @param param 模糊查询条件
     * @return      查询总命中数
     */
    Long fuzzyLogicTemplatesHitByCondition(IndexTemplateDTO param);

    /**
     * 根据名字查询
     * @param templateName 模板名字
     * @return list
     */
    List<IndexTemplate> listLogicTemplateByName(String templateName);

    /**
     * 查询指定的逻辑模板
     * @param logicTemplateId 模板id
     * @return 模板信息  不存在返回null
     */
    IndexTemplate getLogicTemplateById(Integer logicTemplateId);

    /**
     * 删除逻辑模板
     *
     * @param logicTemplateId 模板id
     * @param operator        操作人
     * @return result
     * @throws AdminOperateException
     */
    Result<Void> delTemplate(Integer logicTemplateId, String operator) throws AdminOperateException;

    /**
     * 校验模板参数是否合法
     * @param param 参数
     * @param operation 操作
     * @return result
     */
    Result<Void> validateTemplate(IndexTemplateDTO param, OperationEnum operation, Integer projectId);

    /**
     * 编辑逻辑模板
     * @param param 参数
     * @param operator 操作人
     * @return result
     * @throws AdminOperateException 操作es失败
     */
    Result<Void> editTemplate(IndexTemplateDTO param, String operator, Integer projectId) throws AdminOperateException;

    /**
     * 添加逻辑模板而不需要参数校验
     * @param param 索引逻辑模板参数
     * @return result
     */
    Result<Void> addTemplateWithoutCheck(IndexTemplateDTO param) throws AdminOperateException;

    /**
     * 获取模板配置信息
     * @param logicTemplateId 逻辑模板id
     * @return 配置信息  不存在返回null
     */
    IndexTemplateConfig getTemplateConfig(Integer logicTemplateId);

    /**
     * 更新模板配置
     * @param configDTO  配置参数
     * @param operator 操作人
     * @return result
     */
    Result<Void> updateTemplateConfig(IndexTemplateConfigDTO configDTO, String operator);

    /**
     * 记录模板配置
     *
     * @param indexTemplateConfig 索引模板配置
     * @return result
     */
    Result<Void> insertTemplateConfig(IndexTemplateConfig indexTemplateConfig);

    /**
     * 更新模板配置
     * @param logicTemplateId  逻辑模板id
     * @param factor  factor
     * @param operator 操作人
     */
    void upsertTemplateShardFactor(Integer logicTemplateId, Double factor, String operator);

    /**
     * 更新模板配置
     * @param logicTemplateId  逻辑模板id
     * @param factor  factor
     * @param operator 操作人
     */
    void updateTemplateShardFactorIfGreater(Integer logicTemplateId, Double factor, String operator);

    /**
     * 判断模板是否存在
     * @param logicTemplateId 逻辑模板id
     * @return true/false
     */
    boolean exist(Integer logicTemplateId);

    /**
     * 获取全部逻辑模板
     * @return list
     */
    List<IndexTemplate> listAllLogicTemplates();

    List<IndexTemplate> listAllLogicTemplatesWithCache();

    /**
     * 获取全部逻辑模板
     * @return map，key-逻辑模板ID，value-逻辑模板
     */
    Map<Integer, IndexTemplate> getAllLogicTemplatesMap();

    /**
     * 根据列表获取逻辑模板
     * @return list
     */
    List<IndexTemplate> listLogicTemplatesByIds(List<Integer> logicTemplateIds);

    /**
     * 根据列表获取逻辑模板
     * @return map，key-逻辑模板ID，value-逻辑模板
     */
    Map<Integer, IndexTemplate> getLogicTemplatesMapByIds(List<Integer> logicTemplateIds);

    /**
     * 根据projectId查询模板
     * @param projectId projectId
     * @return list
     */
    List<IndexTemplate> listProjectLogicTemplatesByProjectId(Integer projectId);

    /**
     * 获取所有逻辑集群对应逻辑模板
     * @param logicClusterId 逻辑集群ID
     * @return
     */
    List<IndexTemplate> listLogicClusterTemplates(Long logicClusterId);

    /**
     * 获取模板具体的物理索引
     * @param projectId projectId
     * @return result
     */
    Result<List<Tuple<String, String>>> listLogicTemplatesByProjectId(Integer projectId);

    /**
     * 模板移交
     *
     * @param logicId         模板id
     * @param sourceProjectId 原项目
     * @param tgtProjectId    projectId
     * @param operator        操作人
     * @return Result
     * @throws AdminOperateException
     */
    Result<Void> turnOverLogicTemplate(Integer logicId, Integer sourceProjectId, Integer tgtProjectId,
                                        String operator) throws AdminOperateException;

    /**
     * 获取每个模板的部署个数
     * @return map
     */
    Map<Integer, Integer> getAllLogicTemplatesPhysicalCount();

    /**
     * 获取type
     * @param logicId 模板id
     * @return list
     */
    List<IndexTemplateType> listLogicTemplateTypes(Integer logicId);

    /**
     * 修改模板名称
     * @param param 参数
     * @param operator 操作人
     * @return result
     * @throws AdminOperateException
     */
    Result<Void> editTemplateName(IndexTemplateDTO param, String operator) throws AdminOperateException;

    /**
     * 只更新本地db 不同步更新es
     * @param param
     * @return
     * @throws AdminOperateException
     */
    Result<Void> editTemplateInfoTODB(IndexTemplateDTO param) throws AdminOperateException;

    /**
     * 获取APP有权限的集群下的所有逻辑模板.
     * @param projectId APP的id
     * @return list
     */
    List<IndexTemplate> listTemplatesByHasAuthCluster(Integer projectId);

    /**
     * 获取APP在指定逻辑集群下有权限的逻辑模板.
     * @param projectId project的id
     * @param logicClusterId 逻辑集群ID
     * @return list
     */
    List<IndexTemplate> listHasAuthTemplatesInLogicCluster(Integer projectId, Long logicClusterId);

    /**
     * 获取所有的逻辑模板列表信息（带有逻辑集群和物理模板）
     * @return
     */
    List<IndexTemplateLogicWithClusterAndMasterTemplate> listLogicTemplatesWithClusterAndMasterTemplate();

    /**
     * 获取指定逻辑模板（带有逻辑集群和物理模板）
     * @param logicTemplateId 逻辑模板id
     */
    IndexTemplateLogicWithClusterAndMasterTemplate getLogicTemplateWithClusterAndMasterTemplate(Integer logicTemplateId);
    String getMaterClusterPhyByLogicTemplateId(Integer logicTemplateId);

    /**
     * 获取指定逻辑模板列表信息（带有逻辑集群和物理模板）
     * @param logicTemplateIds 逻辑模板列表
     * @return
     */
    List<IndexTemplateLogicWithClusterAndMasterTemplate> listLogicTemplatesWithClusterAndMasterTemplate(Set<Integer> logicTemplateIds);

    /**
     * 获取指定逻辑模板列表信息（带有逻辑集群和物理模板）
     * @param logicTemplateIds 逻辑模板列表
     * @return map, key-逻辑模板ID, value-带有逻辑集群和物理模板信息的逻辑模板对象
     */
    Map<Integer, IndexTemplateLogicWithClusterAndMasterTemplate> getLogicTemplatesWithClusterAndMasterTemplateMap(Set<Integer> logicTemplateIds);

    /**
     * 获取指定集群下的逻辑模板（带有逻辑集群和物理模板）
     * @param logicClusterIds 逻辑集群id列表
     */
    List<IndexTemplateLogicWithClusterAndMasterTemplate> listLogicTemplateWithClusterAndMasterTemplateByClusters(Set<Long> logicClusterIds);

    /**
     * 获取指定集群下的逻辑模板（带有逻辑集群和物理模板）
     * @param logicClusterId 逻辑集群id
     */
    List<IndexTemplateLogicWithClusterAndMasterTemplate> listLogicTemplateWithClusterAndMasterTemplateByCluster(Long logicClusterId);

    /**
     * 获取单个逻辑模板逻辑集群相关信息
     * @param logicTemplateId 逻辑模板ID
     * @return
     */
    IndexTemplateWithCluster getLogicTemplateWithCluster(Integer logicTemplateId);

    /**
     * 查询模板资源信息
     * @param logicTemplateIds 逻辑模板ID列表
     * @return
     */
    List<IndexTemplateWithCluster> listLogicTemplateWithClusters(Set<Integer> logicTemplateIds);

    /**
     * 查询模板资源信息
     * @return 带有逻辑集群的所有逻辑模板列表
     */
    List<IndexTemplateWithCluster> listAllLogicTemplateWithClusters();

    /**
     * 查询模板资源信息
     * @param logicClusterId 逻辑集群ID
     * @return List<IndexTegetAllLogicClustersmplateLogicClusterMeta> 逻辑模板列表
     */
    List<IndexTemplateWithCluster> listLogicTemplateWithClustersByClusterId(Long logicClusterId);

    /**
     * 获取所有带有物理模板详情的逻辑模板列表
     * @return
     */
    List<IndexTemplateWithPhyTemplates> listAllLogicTemplateWithPhysicals();

    /**
     * 获取指定的带有物理模板详情的逻辑模板列表
     * @return
     */
    List<IndexTemplateWithPhyTemplates> listLogicTemplateWithPhysicalsByIds(Set<Integer> logicTemplateIds);

    /**
     * 根据逻辑模板ID获取对应的物理模板详情
     * @param logicTemplateId 逻辑模板ID
     * @return
     */
    IndexTemplateWithPhyTemplates getLogicTemplateWithPhysicalsById(Integer logicTemplateId);

    /**
     * 获取指定数据中的模板信息
     *
     * @return list
     */
    List<IndexTemplateWithPhyTemplates> listTemplateWithPhysical();

    /**
     * 修改禁读状态
     * @param logicId 逻辑模板
     * @param blockRead  是否禁读
     * @param operator  操作人
     * @return
     */
    Result<Void> updateBlockReadState(Integer logicId, Boolean blockRead, String operator);

    /**
     * 修改禁写状态
     * @param logicId 逻辑模板
     * @param blockWrite  是否禁读
     * @param operator  操作人
     * @return
     */
    Result<Void> updateBlockWriteState(Integer logicId, Boolean blockWrite, String operator);

    Result updateTemplateWriteRateLimit(ConsoleTemplateRateLimitDTO consoleTemplateRateLimitDTO) throws ESOperateException;

    Result<Void> preCheckTemplateName(String templateName);

    /**
     * 获取指定regionId下的所有模板列表
     * @param regionId  regionId
     * @return    Result<List<IndexTemplate>>
     */
    Result<List<IndexTemplate>> listByRegionId(Integer regionId);

    List<IndexTemplateWithCluster> convert2WithCluster(List<IndexTemplateWithPhyTemplates> indexTemplateWithPhyTemplates);

    /**
     * 根据逻辑集群id 列表获取逻辑模板列表
     * @param resourceIds
     * @return
     */
    List<IndexTemplate> listByResourceIds(List<Long> resourceIds);

    /**
     * 获取项目id通过模板逻辑id
     *
     * @param templateLogicId 模板逻辑id
     * @return {@code Integer}
     */
    Integer getProjectIdByTemplateLogicId(Integer templateLogicId);

    IndexTemplatePO getLogicTemplatePOById(Integer logicId);

    boolean update(IndexTemplatePO editTemplate);

    int batchChangeHotDay(Integer days, List<Integer> templateIdList);

    String getNameByTemplateLogicId(Integer logicTemplateId);
    
    List<Integer> getLogicTemplateIdListByProjectId(Integer projectId);

}