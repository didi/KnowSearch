package com.didichuxing.datachannel.arius.admin.core.notify.info.template;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyAlias;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物理模板别名变更提示
 * @author wangshu
 * @date 2020/09/02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateAliasesChangedNotifyInfo implements NotifyInfo {
    /**
     * APP ID
     */
    private Integer               appId;

    /**
     * 模板名称
     */
    private String                templateName;

    /**
     * 变更前模板别名
     */
    private IndexTemplatePhyAlias beforeChangedTemplateAliases;

    /**
     * 变更后模板别名
     */
    private IndexTemplatePhyAlias afterChangedTemplateAliases;

    @Override
    public String getBizId() {
        return String.valueOf(appId);
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        return "Arius搜索平台检测到APPID[\"" + this.appId + "\"]负责的索引[\"" + this.templateName + "\"] 别名发生了变更，请知晓。"
               + "变更前，别名详情内容为：" + JSON.toJSONString(beforeChangedTemplateAliases.toAliasesJSON()) + "<br>变更后，别名详情内容为："
               + JSON.toJSONString(afterChangedTemplateAliases.toAliasesJSON());
    }
}
