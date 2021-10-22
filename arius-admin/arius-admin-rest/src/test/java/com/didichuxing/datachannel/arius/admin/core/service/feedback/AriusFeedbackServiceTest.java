package com.didichuxing.datachannel.arius.admin.core.service.feedback;

import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserLoginRecordService;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author d06679
 * @date 2019-07-11
 */
public class AriusFeedbackServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private AriusUserLoginRecordService ariusUserLoginRecordService;

    @Test
    public void save() {
    }

    @Test
    public void isFirstLogin() {
        ariusUserLoginRecordService.isFirstLogin("fengqiongfeng_v");
    }

    @Test
    public void isTodayFirstLogin() {
        ariusUserLoginRecordService.isTodayFirstLogin("fengqiongfeng_v");
    }


}