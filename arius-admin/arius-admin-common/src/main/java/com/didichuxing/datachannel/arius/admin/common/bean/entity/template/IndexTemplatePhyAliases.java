package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class IndexTemplatePhyAliases implements Serializable {
    private JSONObject aliases;

    public IndexTemplatePhyAliases(JSONObject aliases) {
        this.aliases = aliases;
    }

    /**
     * Check别名是否存在
     * @param aliasName 别名名称
     * @return
     */
    public boolean isAliasExists(String aliasName) {
        if (StringUtils.isNotBlank(aliasName) && aliases != null) {
            return aliases.containsKey(aliasName);
        }

        return false;
    }

    /**
     * 删除别名
     * @param aliasName 别名名称
     */
    public void removeAlias(String aliasName) {
        if (StringUtils.isNotBlank(aliasName)) {
            this.aliases.remove(aliasName);
        }
    }

    /**
     * 增加别名
     * @param aliasName 别名名称
     * @param filter Filter
     * @return
     */
    public boolean putAlias(String aliasName, JSONObject filter) {
        if (StringUtils.isNotBlank(aliasName) && filter != null) {
            this.aliases.put(aliasName, filter);
            return true;
        }

        return false;
    }

    /**
     * 获取别名列表信息
     * @return
     */
    public List<IndexTemplatePhyAlias> parseTemplateAliases() {
        List<IndexTemplatePhyAlias> parsedAliases = new ArrayList<>();
        if (aliases != null) {
            for (Map.Entry<String, Object> alias : aliases.entrySet()) {
                IndexTemplatePhyAlias templateAlias = new IndexTemplatePhyAlias();
                templateAlias.setAlias(alias.getKey());
                parsedAliases.add(templateAlias);
            }
        }

        return parsedAliases;
    }
}
