package com.didichuxing.datachannel.arius.admin.biz.template;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalCopyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalUpgradeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplatePhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import java.util.List;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

public interface TemplatePhyManager {

    /**
     * 元数据校验
     *
     * @return
     */
    boolean checkMeta();

    /**
     * 元数据同步
     *
     * @param physicalId
     * @return
     */
    void syncMeta(Long physicalId, int retryCount) throws ESOperateException;

    /**
     * 删除
     *
     * @param physicalId 物理模板id
     * @param operator   操作人
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    Result<Void> delTemplate(Long physicalId, String operator) throws ESOperateException;

    /**
     * 删除
     *
     * @param logicId  id
     * @param operator 操作人
     * @return result
     * @throws ESOperateException e
     */
    Result<Void> delTemplateByLogicId(Integer logicId, String operator) throws ESOperateException;

    /**
     * 升版本
     * <p>
     * 1、修改数据库中的版本号
     * 2、删除原版本明天的索引,如果指定了rack就按着rack创建,否则在源rack上创建
     * 3、创建新版本明天的索引,按着模板rack创建
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    Result<Void> upgradeTemplate(TemplatePhysicalUpgradeDTO param, String operator) throws ESOperateException;

    Result<Boolean> upgradeMultipleTemplate(List<TemplatePhysicalUpgradeDTO> params, String operator) throws ESOperateException;

    Result<Void> rolloverUpgradeTemplate(TemplatePhysicalUpgradeDTO param, String operator) throws ESOperateException;

    /**
     * 复制 只在目标集群建立模板即可,模板管理的资源都是与逻辑模板id管理,与物理模板没有关系
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    Result<Void> copyTemplate(TemplatePhysicalCopyDTO param, String operator) throws AdminOperateException;

    /**
     * 编辑
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    Result<Void> editTemplate(IndexTemplatePhyDTO param, String operator) throws ESOperateException;

    Result<Boolean> editMultipleTemplate(List<IndexTemplatePhyDTO> params,
                                         String operator) throws ESOperateException;

    /**
     * 校验物理模板信息
     *
     * @param param     参数
     * @param operation 操作
     * @return result
     */
    Result<Void> validateTemplate(IndexTemplatePhyDTO param, OperationEnum operation);

    /**
     * 批量校验物理模板信息
     *
     * @param params    参数
     * @param operation 操作
     * @return result
     */
    Result<Void> validateTemplates(List<IndexTemplatePhyDTO> params, OperationEnum operation);

    /**
     * 批量新增物理模板
     *
     * @param logicId       逻辑模板id
     * @param physicalInfos 物理模板信息
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    Result<Void> addTemplatesWithoutCheck(Integer logicId,
                                          List<IndexTemplatePhyDTO> physicalInfos) throws AdminOperateException;

    /**
     * 新建
     *
     * @param param 模板参数
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    Result<Long> addTemplateWithoutCheck(IndexTemplatePhyDTO param) throws AdminOperateException;

    /**
     * 修改由于逻辑模板修改而物理模板需要同步修改的属性
     * <p>
     * 目前有:
     * expression
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    Result<Void> editTemplateFromLogic(IndexTemplateDTO param, String operator) throws ESOperateException;

    /**
     * 主从切换
     *
     * @param logicId                逻辑模板id
     * @param expectMasterPhysicalId 期望的主
     * @param operator               操作人
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    Result<Void> switchMasterSlave(Integer logicId, Long expectMasterPhysicalId, String operator);

    /**
     * 更新模板的rack和shard
     *
     * @param physicalId 物理模板的id
     * @param tgtRack    rack
     * @return result
     * @throws ESOperateException
     */
    Result<Void> editTemplateRackWithoutCheck(Long physicalId, String tgtRack, String operator,
                                              int retryCount) throws ESOperateException;

    /**
     * 升级模板
     *
     * @param physicalId physicalId
     * @return reuslt
     */

    Result<Void> upgradeTemplateVersion(Long physicalId, String operator, int retryCount) throws ESOperateException;

    /**
     *
     * @param param
     * @param operator
     * @param retryCount
     * @return
     * @throws ESOperateException
     */
    Result<Void> editTemplateWithoutCheck(IndexTemplatePhyDTO param, String operator,
                                          int retryCount) throws ESOperateException;

    /**
     * 根据逻辑模板和热数据保存天数或者过期天数来获取需要迁移到冷存的索引名称列表或者需要删除的索引名称列表
     * @param physicalWithLogic 逻辑物理模板
     * @param days 热数据保存天数
     * @return 索引名称列表
     */
    Tuple</*存放冷存索引列表*/Set<String>,/*存放热存索引列表*/Set<String>> getHotAndColdIndexByBeforeDay(IndexTemplatePhyWithLogic physicalWithLogic, int days);

    /**
     * 获取指定天数外的索引列表
     * @param physicalWithLogic 逻辑物理模板
     * @param days  天数
     * @return 索引名称列表
     */
    Set<String> getIndexByBeforeDay(IndexTemplatePhyWithLogic physicalWithLogic, int days);


    /**
     * 获取带有App权限信息的物理模板列表
     * @param projectId 当前登录projectId
     */
    List<ConsoleTemplatePhyVO> getConsoleTemplatePhyVOS(IndexTemplatePhyDTO param, Integer projectId);

    /**
     * 根据项目获取有管理权限的物理模板
     */
    List<String> getTemplatePhyNames(Integer projectId);

    /**
     * 获取物理模板可复制的物理集群名称列表, 仅支持不同集群间模板复制
     */
    List<String> getCanCopyTemplatePhyClusterPhyNames(Long templatePhyId);

    /**
     * 根据逻辑模板Id获取多个物理模板信息
     * @param logicId    逻辑集群Id
     * @return           List<IndexTemplatePhysicalVO>
     */
    Result<List<IndexTemplatePhysicalVO>> getTemplatePhies(Integer logicId);
}