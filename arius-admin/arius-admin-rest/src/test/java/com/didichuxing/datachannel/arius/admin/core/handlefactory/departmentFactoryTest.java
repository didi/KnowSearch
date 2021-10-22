package com.didichuxing.datachannel.arius.admin.core.handlefactory;

import com.didichuxing.datachannel.arius.admin.common.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.remote.protocol.LoginProtocolHandle;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.didichuxing.datachannel.arius.admin.common.component.BaseExtendFactory;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.remote.employee.EmployeeHandle;
import com.didichuxing.datachannel.arius.admin.remote.employee.content.EmployeeTypeEnum;
import com.didichuxing.datachannel.arius.admin.remote.protocol.content.LoginProtocolTypeEnum;
import com.didichuxing.datachannel.arius.admin.remote.storage.FileStorageHandle;
import com.didichuxing.datachannel.arius.admin.remote.storage.content.FileStorageTypeEnum;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author linyunan
 * @date 2021-04-26
 */
public class departmentFactoryTest extends AriusAdminApplicationTests {

    @Autowired
    private HandleFactory handleFactory;

    @Test
    public void getByHandlerNamePerTest() {

        FileStorageHandle storageService = BaseExtendFactory.getByClassNamePer(FileStorageTypeEnum.DEFAULT.getType(),
            FileStorageHandle.class);

        BaseHandle clusterOpOffline01 = BaseExtendFactory.getByClassNamePer("clusterOpOffline", BaseHandle.class);
        BaseHandle clusterOpOffline = handleFactory.getByHandlerNamePer("clusterOpOffline");

        BaseHandle employee = handleFactory.getByHandlerNamePer(EmployeeTypeEnum.DEFAULT.getType());

        EmployeeHandle employee01 = BaseExtendFactory.getByClassNamePer(EmployeeTypeEnum.DEFAULT.getType(),
            EmployeeHandle.class);

        LoginProtocolHandle protocol01 = BaseExtendFactory.getByClassNamePer(LoginProtocolTypeEnum.DEFAULT.getType(),
            LoginProtocolHandle.class);

        Assert.assertNotNull(clusterOpOffline01);
    }

}