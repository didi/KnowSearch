package com.didichuxing.datachannel.arius.admin.core.notify.info.dcdr;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zengqiao
 * @date 20/10/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DcdrSwitchMasterNotifyInfo implements NotifyInfo {

    private IndexTemplateLogic templateLogic;

    private Long               expectMasterPhysicalId;

    private Result             result;

    private String             operator;

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
        msgBuilder.append("【DCDR主从切换通知】\n");
        msgBuilder.append("模板名称：").append(templateLogic.getName()).append("\n");
        msgBuilder.append("期望主模板：").append(expectMasterPhysicalId).append("\n");
        msgBuilder.append("操作人：").append(operator).append("\n");
        msgBuilder.append("操作结果：").append(result.success() ? "操作成功" : "操作失败").append("\n");
        msgBuilder.append("发送时间：").append(AriusDateUtils.date2Str(new Date(), null)).append("\n");
        msgBuilder.append("执行信息：").append(result.getMessage()).append("\n");
        return msgBuilder.toString();
    }
}