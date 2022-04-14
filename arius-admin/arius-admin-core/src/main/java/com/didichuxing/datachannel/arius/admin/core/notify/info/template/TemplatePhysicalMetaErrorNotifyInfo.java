package com.didichuxing.datachannel.arius.admin.core.notify.info.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
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
public class TemplatePhysicalMetaErrorNotifyInfo implements NotifyInfo {

    private IndexTemplatePhy templatePhysical;

    private String           errMsg;

    @Override
    public String getBizId() {
        return String.valueOf(templatePhysical.getId());
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("【物理模板信息异常】\n");
        msgBuilder.append("模板ID：" + templatePhysical.getId() + "\n");
        msgBuilder.append("模板名字：" + templatePhysical.getName() + "\n");
        msgBuilder.append("异常信息：" + errMsg + "\n");
        msgBuilder.append("发送时间：" + AriusDateUtils.date2Str(new Date(), null) + "\n");
        return msgBuilder.toString();
    }
}