package com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateSchemaDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateSchemaOptimizeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithMapping;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateMappingVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.common.mapping.Field;
import java.util.List;
import java.util.Set;

/**
 * 逻辑模板的mapping服务；对我暴露的是field对象，并屏蔽底层的mapping信息、部署信息
 * @author zhognhua
 */
public interface TemplateLogicMappingManager {

    /**
     * 查询指定的逻辑模板 带有Mapping信息
     * @param logicId 模板id
     * @throws Exception
     * @return 模板信息  不存在返回null
     */
    Result<IndexTemplateWithMapping> getTemplateWithMapping(Integer logicId);

    /**
     * 更新
     *
     * @param logicId   模板id
     * @param fields    fields
     * @param projectId
     * @param operator
     * @return result
     */
    Result<Void> updateFields(Integer logicId, List<Field> fields, Set<String> removeFields, Integer projectId,
                              String operator);

    /**
     * 校验模板field
     *
     * @param logicId 模板id
     * @param fields 属性列表
     * @return Result
     */
    Result<Void> checkFields(Integer logicId, List<Field> fields);

    /**
     * 更新
     * @param logicId 模板id
     * @param ariusTypeProperty mapping
     * @return result
     */
    Result<Void> updateMappingForNew(Integer logicId, AriusTypeProperty ariusTypeProperty) throws ESOperateException;

    /**
     * updateProperties
     * @param logicId
     * @param properties
     * @param operator
     * @return
     */
    Result<Void> updateProperties(Integer logicId, List<AriusTypeProperty> properties, String operator);

    /**
     * field装AriusTypeProperty
     * @param fields fields
     * @return str
     */
    AriusTypeProperty fields2Mapping(List<Field> fields);



    /**
     * mapping优化
     * @param optimizeDTO dto
     * @param operator 操作人
     * @return result
     */
    Result<Void> modifySchemaOptimize(ConsoleTemplateSchemaOptimizeDTO optimizeDTO, String operator);

    /**
     * 修改模板schema
     *
     * @param schemaDTO       schema
     * @param operator        操作人
     * @param projectId
     * @return result
     */
    Result<Void> editMapping(ConsoleTemplateSchemaDTO schemaDTO, String operator,
                             Integer projectId) throws AdminOperateException;

    /**
     * 获取模板schema
     * @param logicId 模板id
     * @return result
     */
    Result<TemplateMappingVO> getSchema(Integer logicId);
}