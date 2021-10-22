package com.didichuxing.datachannel.arius.admin;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class BaseContextTests {
    protected static String prefix;

    @BeforeAll
    public static void init() throws IOException {
        Properties properties = new Properties();
        File file = new File("address.properties");
        String address;
        if (!file.exists()) {
            address = "127.0.0.1:8010";
        } else {
            FileInputStream inputStream = new FileInputStream(file);
            properties.load(inputStream);
            address = properties.getProperty("address");
        }
        prefix = "http://" + address + "/admin/api";
        AriusClient.setPrefix(prefix);
    }

    @Test
    public void contextLoads() {
    }

}
