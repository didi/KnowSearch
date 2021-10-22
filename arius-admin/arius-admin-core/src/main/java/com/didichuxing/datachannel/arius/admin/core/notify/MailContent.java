package com.didichuxing.datachannel.arius.admin.core.notify;

import java.util.List;

/**
 * @author d06679
 * @date 2019-07-22
 */
public interface MailContent {

    String ARIUS_MAIL_NOTIFY = "【Arius服务中心通知】";

    String getSubject();

    String getHtmlMsg();

    List<String> getToAddrList();

    default List<String> getCcAddrList() {
        return null;
    }

}
