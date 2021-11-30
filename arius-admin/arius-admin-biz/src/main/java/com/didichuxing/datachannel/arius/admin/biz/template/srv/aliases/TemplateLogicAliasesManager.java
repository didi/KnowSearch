package com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.ConsoleAliasDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.ConsoleLogicTemplateAliasesDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.ConsoleLogicTemplateDeleteAliasesDTO;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateAlias;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyAlias;

import java.util.List;

public interface TemplateLogicAliasesManager {

    /**
     * 获取别名
     * @return list
     */
    List<IndexTemplateAlias> listAlias();

    /**
     * 获取别名
     * @return list
     */
    List<IndexTemplateAlias> listAlias(List<IndexTemplateLogicWithPhyTemplates> templateLogicList);

    /**
     * 获取别名
     * @param logicId logicId
     * @return list
     */
    List<IndexTemplateAlias> getAliasesById(Integer logicId);

    /**
     * getAliases
     * @param logicId
     * @return
     */
    Result<List<IndexTemplatePhyAlias>> getAliases(Integer logicId);

    /**
     * createAliases
     * @param aliases
     * @param operator
     * @return
     */
    Result<Void> createAliases(ConsoleLogicTemplateAliasesDTO aliases, String operator);

    /**
     * modifyAliases
     * @param aliases
     * @param operator
     * @return
     */
    Result<Void> modifyAliases(ConsoleLogicTemplateAliasesDTO aliases, String operator);

    /**
     * deleteTemplateAliases
     * @param deleteAliasesDTO
     * @param operator
     * @return
     */
    Result<Void> deleteTemplateAliases(ConsoleLogicTemplateDeleteAliasesDTO deleteAliasesDTO, String operator);

    /**
     * getAllTemplateAliasesByAppid
     * @param appId
     * @return
     */
    Result<List<Tuple<String/*index*/, String/*aliases*/>>> getAllTemplateAliasesByAppid(Integer appId);

    /**
     * 根据逻辑模板ID获取对应别名详情列表
     * @param logicId 逻辑ID
     * @return
     */
    Result<List<IndexTemplatePhyAlias>> fetchTemplateAliasesByLogicId(Integer logicId);

    /**
     * 创建逻辑索引模板别名
     * @param logicId 逻辑模板ID
     * @param aliases 别名列表
     * @return
     */
    Result<Void> createTemplateAliases(Integer logicId, List<ConsoleAliasDTO> aliases);

    /**
     * 更新逻辑模板别名
     * @param logicId 逻辑模板ID
     * @param aliases 别名列表
     * @return
     */
    Result<Void> modifyTemplateAliases(Integer logicId, List<ConsoleAliasDTO> aliases);

    /**
     * 删除模板别名列表
     * @param logicId 逻辑模板ID
     * @param aliases 别名列表
     * @return
     */
    Result<Void> deleteTemplateAliases(Integer logicId, List<String> aliases);
}
