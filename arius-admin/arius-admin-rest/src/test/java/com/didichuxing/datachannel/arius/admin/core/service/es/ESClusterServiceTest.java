package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.impl.ESClusterLogicServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ESClusterServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private ESClusterDAO esClusterDAO;

    @Autowired
    private ESClusterLogicServiceImpl service = new ESClusterLogicServiceImpl();

    @Test
    public void test() {

    }
}
