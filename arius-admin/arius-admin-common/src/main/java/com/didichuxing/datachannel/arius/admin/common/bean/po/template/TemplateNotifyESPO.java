package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateNotifyESPO extends BaseESPO {
    int    logicTemplateId;

    int    projectId;

    String templateName;

    String zeroDate;

    int    rate;

    int    notifyNu;

    @Override
    public String getKey() {
        return logicTemplateId + "#" + zeroDate + "#" + rate;
    }

    @Override
    public String getRoutingValue() {
        return null;
    }
}