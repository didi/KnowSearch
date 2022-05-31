package com.didichuxing.datachannel.arius.admin.biz.template;

import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicAggregate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithLabels;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;

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
     * 校验所有逻辑模板元数据信息
     *
     * @return
     */
    boolean checkAllLogicTemplatesMeta();

    /**
     * 获取模板信息
     * @param excludeLabelIds 排除的Label ID列表
     * @param includeLabelIds 包含的Label ID列表
     * @return list
     */
    List<IndexTemplateWithLabels> getByLabelIds(String includeLabelIds, String excludeLabelIds);

    /**
     * 获取最近访问该模板的APP
     *
     * @param logicId logicId
     * @return result
     */
    List<ProjectBriefVO> getLogicTemplateProjectAccess(Integer logicId);

    /**
     * 获取模板的标签信息
     * @param logicId 模板id
     * @return label
     */
    IndexTemplateWithLabels getLabelByLogicId(Integer logicId);

    /**
     * 新建逻辑模板 无参数校验
     *
     * @param param    模板信息
     * @param operator 操作人
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    Result<Integer> addTemplateWithoutCheck(IndexTemplateDTO param, String operator) throws AdminOperateException;

    /**
     * 新建逻辑模板
     * @param param 模板信息
     * @param operator 操作人
     * @return result
     * @throws AdminOperateException 操作es失败 或者保存物理模板信息失败
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    Result<Integer> createLogicTemplate(IndexTemplateDTO param, String operator) throws AdminOperateException;

    /**
     * 获取所有逻辑模板聚合
     *
     * @param projectId 当前project Id
     * @return
     */
    List<IndexTemplateLogicAggregate> getAllTemplatesAggregate(Integer projectId);

    /**
     * 获取逻辑集群所有逻辑模板聚合
     *
     * @param logicClusterId 逻辑集群ID
     * @param projectId 操作的project Id
     * @return
     */
    List<IndexTemplateLogicAggregate> getLogicClusterTemplatesAggregate(Long logicClusterId, Integer projectId);

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
     * 获取逻辑集群所有逻辑模板列表
     */
    List<ConsoleTemplateVO> getConsoleTemplateVOSForClusterLogic(Long clusterLogicId, Integer projectId);

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

    Result<Void> editTemplate(IndexTemplateDTO param, String operator) throws AdminOperateException;

    Result<Void> delTemplate(Integer logicTemplateId, String operator) throws AdminOperateException;

    /**
     * 模糊（精确）/分页查询模板列表接口
     * @param condition  查询条件
     * @param projectId      项目
     * @return
     */
    PaginationResult<ConsoleTemplateVO> pageGetConsoleTemplateVOS(TemplateConditionDTO condition, Integer projectId);

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
     * @param templateLogicId 逻辑模版id
     * @param status 1 启用，0 禁用
     * @param operator 操作者
     * @return
     */
    Result<Void> switchRolloverStatus(Integer templateLogicId, Integer status, String operator);
    /**
     * 获取创建dcdr链路模板
     * @return
     */
    List<Integer> getHaveDCDRLogicIds();

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
}