package com.didichuxing.datachannel.arius.admin.core.notify;

/**
 * 通知消息接口
 * @author didi
 */
public interface NotifyInfo {

    /**
     * 业务主键，例如用户ID，模板ID，APP ID等；用于标记本次通知事件
     * 用于流控，
     * @return 标志
     */
    String getBizId();

    /**
     * 获取title
     * @return title
     */
    default String getTitle() {
        return NotifyConstant.ARIUS_MAIL_NOTIFY;
    }

    /**
     * 获取内容
     * @return content
     */
    default String getMailContent() {
        return "Arius";
    }

    /**
     * 获取内容
     * @return content
     */
    default String getSmsContent() {
        return "Arius";
    }

    /**
     * 获取内容
     * @return content
     */
    default String getVoiceContent() {
        return "Arius";
    }

}
