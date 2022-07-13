package com.didichuxing.datachannel.arius.admin.base;

import com.didichuxing.datachannel.arius.admin.common.util.YamlUtil;
import com.didichuxing.datachannel.arius.admin.resource.CustomDataSource;
import com.didichuxing.datachannel.arius.admin.util.AriusClient;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;

import java.io.*;

public class BaseContextTest {

    protected static String prefix;

    @BeforeAll
    public static void init() throws IOException {
        initProperties();
        String address = CustomDataSource.testAdminIp + ":" + CustomDataSource.testAdminPort;
        prefix = "http://" + address + "/admin/api";
        AriusClient.setPrefix(prefix);
    }

    public static void initProperties() throws IOException {
        String[] ymlPaths = { "arius-admin-rest", "target", "classes", "application.yml" };
        String path = System.getProperty("user.dir");
        // D:\software\project\IdeaProjects\arius-admin-v2\arius-admin-rest\target\classes\application.yml
        path = path.substring(0, path.lastIndexOf(File.separator) + 1) + StringUtils.join(ymlPaths, File.separator);
        String testPhyClusterIp = YamlUtil.getValue(path, "integrate.test.es.ip");
        if (StringUtils.isEmpty(testPhyClusterIp)) {
            throw new RuntimeException("集成测试需要可用的ES服务ip，请配置文件application.yml中设置：integrate.test.es.ip");
        }
        String testPhyClusterPort = YamlUtil.getValue(path, "integrate.test.es.port");
        if (StringUtils.isEmpty(testPhyClusterPort)) {
            throw new RuntimeException("集成测试需要可用的ES服务port，请配置文件application.yml中设置：integrate.test.es.port");
        }
        String appid = YamlUtil.getValue(path, "integrate.test.appid");
        if (StringUtils.isEmpty(appid)) {
            throw new RuntimeException("集成测试需要appid，请配置文件application.yml中设置：integrate.test.appid");
        }
        String operator = YamlUtil.getValue(path, "integrate.test.operator");
        if (StringUtils.isEmpty(operator)) {
            throw new RuntimeException("集成测试需要operator，请配置文件application.yml中设置：integrate.test.operator");
        }
        String testAdminIp = YamlUtil.getValue(path, "integrate.test.admin.ip");
        if (StringUtils.isEmpty(testAdminIp)) {
            throw new RuntimeException("集成测试需要可用的Admin服务ip，请配置文件application.yml中设置：integrate.test.admin.ip");
        }
        String testAdminPort = YamlUtil.getValue(path, "integrate.test.admin.port");
        if (StringUtils.isEmpty(testAdminPort)) {
            throw new RuntimeException("集成测试需要可用的Admin服务port，请配置文件application.yml中设置：integrate.test.admin.port");
        }
        CustomDataSource.testAdminIp = testAdminIp;
        CustomDataSource.testAdminPort = testAdminPort;
        CustomDataSource.testPhyClusterIp = testPhyClusterIp;
        CustomDataSource.testPhyClusterPort = Integer.valueOf(testPhyClusterPort);
        CustomDataSource.projectId = Integer.valueOf(appid);
        CustomDataSource.operator = operator;
    }
}