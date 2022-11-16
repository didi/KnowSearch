package com.didichuxing.datachannel.arius.admin.biz.template;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.*;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateIncrementalSettingsDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicAggregate;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.*;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.AmsRemoteException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;

/**
 * 逻辑模板管理Biz类
 *
 * @author wangshu
 * @date 2020/09/11
 */
@Component
public interface TemplateLogicManager {

    /**
     * 默认模板价值分
     */
    int DEFAULT_TEMPLATE_VALUE = 61;

    /**
     * 获取最近访问该模板的project
     *
     * @param logicId logicId
     * @return result
     */
    List<ProjectBriefVO> getLogicTemplateProjectAccess(Integer logicId) throws AmsRemoteException;

    /**
     * 新建逻辑模板
     * @param param 模板信息
     * @param operator 操作人
     * @param projectId projectId
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    Result<Void> create(IndexTemplateWithCreateInfoDTO param, String operator,
                        Integer projectId);

    /**
     * 获取所有逻辑模板聚合
     *
     * @param projectId 当前projectId
     * @return
     */
    List<IndexTemplateLogicAggregate> getAllTemplatesAggregate(Integer projectId);



    /**
     * 拼接集群名称
     * @param logicClusters 逻辑集群详情列表
     * @return
     */
    String jointCluster(List<ClusterLogic> logicClusters);

    /**
     *
     * @param aggregates 聚合列表
     * @return
     */
    List<ConsoleTemplateVO> fetchConsoleTemplates(List<IndexTemplateLogicAggregate> aggregates);

    /**
     * 获取模板VO
     * @param aggregate 模板聚合
     * @return
     */
    ConsoleTemplateVO fetchConsoleTemplate(IndexTemplateLogicAggregate aggregate);

    /**
     * 获取逻辑索引列表
     */
    List<ConsoleTemplateVO> getConsoleTemplatesVOS(Integer projectId);

    /**
     * 根据项目和权限类型获取模板信息
     * @param projectId                 项目Id
     * @param authType              权限类型
     * @see   ProjectTemplateAuthEnum
     * @return
     */
    List<IndexTemplate> getTemplatesByProjectIdAndAuthType(Integer projectId, Integer authType);

    /**
     * 根据项目获取有管理\读写\读权限的逻辑模版
     */
    List<String> getTemplateLogicNames(Integer projectId);

    /**
     * 获取模板的业务类型
     */
    Map<Integer, String> getDataTypeCode2DescMap();

    Result<Void> editTemplate(IndexTemplateDTO param, String operator, Integer projectId);

    Result<Void> delTemplate(Integer logicTemplateId, String operator, Integer projectId) throws AdminOperateException;

    /**
     * 模糊（精确）/分页查询模板列表接口
     * @param condition  查询条件
     * @param projectId      项目
     * @return
     */
    PaginationResult<ConsoleTemplateVO> pageGetConsoleTemplateVOS(TemplateConditionDTO condition,
                                                                  Integer projectId) throws NotFindSubclassException;

    /**
     * 校验创建模板名称是否合法
     * @param templateName   模板名称
     * @return
     */
    Result<Void> checkTemplateValidForCreate(String templateName);

    /**
     * 校验模板mapping可否编辑
     * @param templateId
     * @return
     */
    Result<Boolean> checkTemplateEditMapping(Integer templateId);

    /**
     * 更改逻辑模版的rollover能力
     *
     * @param templateLogicId 逻辑模版id
     * @param status          1 启用，0 禁用
     * @param operator        操作者
     * @param projectId
     * @return
     */
    Result<Void> switchRolloverStatus(Integer templateLogicId, Integer status, String operator, Integer projectId);



    /**
     * 校验模板是否可以使用索引模板的相关服务，例如是否可以编辑mapping,setting
     * @param templateId 逻辑模板id
     * @param templateSrvId 索引模板服务id
     * @return 校验的结果
     */
    Result<Boolean> checkTemplateEditService(Integer templateId, Integer templateSrvId);

    /**
     * 校验指定projectId能否对指定的逻辑模板进行操作
     * @param logicId 逻辑模板id
     * @param projectId projectId
     * @return result
     */
    Result<Void> checkProjectAuthOnLogicTemplate(Integer logicId, Integer projectId);

    /**
     * 同步dcdr相关信息
     * @param logicId
     * @return
     */
    boolean updateDCDRInfo(Integer logicId);

    /**
     * 全量获取指定物理集群所关联的逻辑模板信息列表
     *
     * @param phyCluster 物理集群名称
     * @return 物理集群下的全量模板信息列表视图
     */
    Result<List<ConsoleTemplateVO>> getTemplateVOByPhyCluster(String phyCluster);

    /**
     * 清除索引
     */
    Result<Void> clearIndices(TemplateClearDTO clearDTO, String operator, Integer projectId);

    /**
     * 执行调整shard 数量
     *
     * @param logicTemplateId 模板id
     * @param shardNum        调整后的shard数量
     * @param projectId
     * @param operator
     * @return 调整结果
     */
    Result<Void> adjustShard(Integer logicTemplateId, Integer shardNum, Integer projectId,
                             String operator) throws AdminOperateException;

    /**
     * 模板升级
     *
     * @param templateId 模板id
     * @param operator   操作者
     * @param projectId
     * @return
     */
    Result<Void> upgrade(Integer templateId, String operator, Integer projectId) throws AdminOperateException;

    Result<List<ConsoleTemplateVO>> listTemplateVOByLogicCluster(String clusterLogicName, Integer projectId);

    Result<List<Tuple<String, String>>> listLogicTemplatesByProjectId(Integer projectId);

    Result<List<TemplateCyclicalRollInfoVO>> getCyclicalRollInfo(Integer logicId);

    Result<ConsoleTemplateRateLimitVO> getTemplateRateLimit(Integer logicId);

    Result<ConsoleTemplateDetailVO> getDetailVoByLogicId(Integer logicId, Integer projectId);

    Result<ConsoleTemplateClearVO> getLogicTemplateClearInfo(Integer logicId) throws AmsRemoteException;

    Result<ConsoleTemplateDeleteVO> getLogicTemplateDeleteInfo(Integer logicId) throws AmsRemoteException;

    Result<Void> updateTemplateWriteRateLimit(ConsoleTemplateRateLimitDTO consoleTemplateRateLimitDTO, String operator,
                                              Integer projectId);
    
    /**
     * 它通过其逻辑 ID 更新模板的健康状况。
     *
     * @param logicId 模板的 logicId。
     * @return 一个布尔值。
     */
    boolean updateTemplateHealthByLogicId(Integer logicId);
    
    /**
     * 用索引模板的写操作。
     *
     * @param templateId 要操作的模板的 id
     * @param status     0：否，1：是
     * @param operator   触发操作的操作员
     * @param projectId  项目编号
     * @return Result<Void>
     */
    Result<Void> blockWrite(Integer templateId, Boolean status, String operator, Integer projectId);
    
    /**
     * 用于索引模板的读取。
     *
     * @param templateId 要操作的模板的 id
     * @param status     0：否，1：是
     * @param operator   触发操作的用户
     * @param projectId  项目编号
     * @return Result<Void>
     */
    Result<Void> blockRead(Integer templateId, Boolean status, String operator, Integer projectId);

    /**
     * 更新模版settings和非分区模版索引的settings(可以用来实现部分模版服务，如异步translog、恢复优先级)
     * @param param 模版增量settings
     * @param operator
     * @param projectId
     * @return
     */
    Result<Void> updateTemplateAndIndexSettings(TemplateIncrementalSettingsDTO param, String operator, Integer projectId) throws AdminOperateException;

    /**
     * 转让模板
     *
     * @param templateId           模板id
     * @param targetProjectId      目标项目id
     * @param targetLogicClusterId 目标逻辑集群id
     * @param phyClusterName       phy集群名称
     * @return {@link Result}<{@link Integer}>
     */
    Result<Integer> transferTemplate(Integer templateId, Integer targetProjectId, Long targetLogicClusterId,
                                     String phyClusterName);

    /**
     * 转让模板检查
     *
     * @param targetCluster        目标集群
     * @param targetLogicClusterId 目标逻辑集群id
     * @param targetProjectId      目标项目id
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> transferTemplateCheck(String targetCluster, Long targetLogicClusterId, Integer targetProjectId);

}