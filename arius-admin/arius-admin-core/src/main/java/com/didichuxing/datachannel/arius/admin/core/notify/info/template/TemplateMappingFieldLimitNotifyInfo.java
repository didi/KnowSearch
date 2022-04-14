package com.didichuxing.datachannel.arius.admin.core.notify.info.template;

import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模版字段长度超标导致无法同步提示
 * @author fengqiongfeng
 * @date 2020/12/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMappingFieldLimitNotifyInfo implements NotifyInfo {

    /**
     * 集群名
     */
    private String  cluster;

    /**
     * 模版名称
     */
    private String  template;

    /**
     * field限制长度
     */
    private Integer fieldLimitSize;

    @Override
    public String getBizId() {
        return this.template;
    }

    @Override
    public String getSmsContent() {
        return getContent();
    }

    private String getContent() {
        StringBuilder content = new StringBuilder();
        content.append("Arius搜索平台检测到集群：").append(cluster).append("中的模版:").append(template).append("字段超过")
            .append(fieldLimitSize).append(",自动同步模版失败,请知晓。");

        return content.toString();
    }

}
