package com.didichuxing.datachannel.arius.admin.core.notify.info.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zengqiao
 * @date 20/10/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateLogicMetaErrorNotifyInfo implements NotifyInfo {

    private IndexTemplateLogic templateLogic;

    private String             errMsg;

    @Override
    public String getBizId() {
        return String.valueOf(templateLogic.getId());
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        StringBuilder msgBuilder = new StringBuilder("");
        msgBuilder.append("【逻辑模板信息异常】\n");
        msgBuilder.append("模板ID：").append(templateLogic.getId()).append("\n");
        msgBuilder.append("模板名字：").append(templateLogic.getName()).append("\n");
        msgBuilder.append("异常信息：").append(errMsg).append("\n");
        msgBuilder.append("发送时间：").append(AriusDateUtils.date2Str(new Date(), null)).append("\n");
        return msgBuilder.toString();
    }
}