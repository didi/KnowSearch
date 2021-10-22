package com.didichuxing.datachannel.arius.admin.core.notify.info.template;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板属性types变更提示
 * @author wangshu
 * @date 2020/09/02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePropertiesTypesChangedNotifyInfo implements NotifyInfo {
    /**
     * APP ID
     */
    private Integer      appId;

    /**
     * 模板名称
     */
    private String       templateName;

    /**
     * 变更前模板properties类型列表
     */
    private List<String> beforeChangedPropertiesTypes;

    /**
     * 变更后模板properties类型列表
     */
    private List<String> afterChangedPropertiesTypes;

    @Override
    public String getBizId() {
        return String.valueOf(appId);
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        return "Arius搜索平台检测到APPID[\"" + this.appId + "\"]负责的索引[\"" + this.templateName
               + "\"] properties types发生了变更，请知晓。" + "变更前，types内容为：" + JSON.toJSONString(beforeChangedPropertiesTypes)
               + "<br>变更后，types内容为：" + JSON.toJSONString(afterChangedPropertiesTypes);
    }
}
