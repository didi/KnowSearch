package com.didichuxing.datachannel.arius.admin.core.notify.info.template;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhySettings;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板Settings变动提示信息
 * @author wangshu
 * @date 2020/09/02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSettingsChangedNotifyInfo implements NotifyInfo {
    /**
     * APP ID
     */
    private Integer                  appId;

    /**
     * 模板名称
     */
    private String                   templateName;

    /**
     * 变动前settings
     */
    private IndexTemplatePhySettings beforeChangedSettings;

    /**
     * 变动后settings
     */
    private IndexTemplatePhySettings afterChangedSettings;

    @Override
    public String getBizId() {
        return String.valueOf(appId);
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        return "Arius搜索平台检测到APPID[\"" + this.appId + "\"]负责的索引[\"" + this.templateName + "\"]settings发生了变更，请知晓。"
               + "变更前，settings内容为：" + JSON.toJSONString(beforeChangedSettings) + "<br>变更后，settings内容为："
               + JSON.toJSONString(afterChangedSettings);
    }

}
