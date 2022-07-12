package com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.ConsoleAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateAlias;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyAlias;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;

import java.util.List;

public interface TemplateLogicAliasManager {

    /**
     * 获取别名
     * @return list
     */
    List<IndexTemplateAlias> listAlias();

    /**
     * 获取别名
     * @return list
     */
    List<IndexTemplateAlias> listAlias(List<IndexTemplateWithPhyTemplates> templateLogicList);


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