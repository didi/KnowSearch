package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 物理模板别名
 * @author wangshu
 * @date 2020/08/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexTemplatePhyAlias implements Serializable {

    /**
     * 模板别名名称
     */
    private String     alias;

    /**
     * 别名过滤器
     */
    private JSONObject filter;

    /**
     * 转换成别名JSON
     * @return
     */
    public JSONObject toAliasesJSON() {
        JSONObject aliases = new JSONObject();
        aliases.put(alias, filter);
        return aliases;
    }
}
