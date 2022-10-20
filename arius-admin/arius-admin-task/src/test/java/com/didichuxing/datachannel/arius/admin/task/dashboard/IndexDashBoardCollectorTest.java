package com.didichuxing.datachannel.arius.admin.task.dashboard;

import com.didichuxing.datachannel.arius.admin.task.config.AriusAdminTaskConfiguration;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author zhangliang
 * @version : IndexDashBoardCollectorTest.java, v 0.1 2022年03月18日 18:27 zhangliang Exp $
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { AriusAdminTaskConfiguration.class })
public class IndexDashBoardCollectorTest {

    private static final ILog LOGGER = LogFactory.getLog(IndexDashBoardCollectorTest.class);

    @Autowired
    ApplicationContext        wac;

    @Test
    public void testBuildBySingleCluster() {
    }

}
