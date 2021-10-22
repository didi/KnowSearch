package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.core.service.app.impl.AppLogicClusterAuthServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.LogicClusterDAO;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class AppLogicClusterAuthServiceTest extends AriusAdminApplicationTests {

    private AppLogicClusterAuthService service = new AppLogicClusterAuthServiceImpl();

    @Autowired
    private AppDAO appDAO;

    @Autowired
    private LogicClusterDAO logicClusterDAO;

    @BeforeEach
    public void init() {

    }

}
