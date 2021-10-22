package com.didichuxing.datachannel.arius.admin.common.util;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.ConsoleAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyAlias;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板DTO转Entity工具类
 * @author wangshu
 * @date 2020/09/03
 */
public class TemplateDTOConvertUtils {
    /**
     * 转换别名列表
     * @param aliasDTOS 别名DTO列表
     * @return
     */
    public static List<IndexTemplatePhyAlias> convertAliases(List<ConsoleAliasDTO> aliasDTOS) {
        List<IndexTemplatePhyAlias> aliases = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(aliasDTOS)) {
            for (ConsoleAliasDTO aliasDTO: aliasDTOS) {
                aliases.add(convertAlias(aliasDTO));
            }
        }
        return aliases;
    }

    /**
     * 转换别名
     * @param aliasDTO 别名DTO
     * @return
     */
    public static IndexTemplatePhyAlias convertAlias(ConsoleAliasDTO aliasDTO) {
        if (aliasDTO != null) {
            IndexTemplatePhyAlias alias = new IndexTemplatePhyAlias();
            alias.setAlias(aliasDTO.getAlias());
            alias.setFilter(aliasDTO.getFilter());
            return alias;
        }
        return null;
    }
}
