//package com.didichuxing.datachannel.arius.admin.config;
//
//import com.didichuxing.datachannel.arius.admin.AriusClient;
//import com.didichuxing.datachannel.arius.admin.BaseContextTests;
//import com.didichuxing.datachannel.arius.admin.RandomFilledBean;
//import com.didichuxing.datachannel.arius.admin.source.AriusDataSource;
//import com.didichuxing.datachannel.arius.admin.Result;
//import com.didichuxing.datachannel.arius.admin.client.bean.dto.config.AriusConfigInfoDTO;
//import com.didichuxing.datachannel.arius.admin.client.constant.config.AriusConfigStatusEnum;
//import com.didichuxing.datachannel.arius.admin.constant.RequestPathOP;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//
//public class ConfigTest extends BaseContextTests {
//
//    @Test
//    public void addConfig() throws IOException {
//        AriusConfigInfoDTO ariusConfigInfoDTO= AriusDataSource.ariusConfigInfoDTOFactory();
//        Result result1 = new AriusClient().post(RequestPathOP.CONFIG_ADD, ariusConfigInfoDTO);
//        Assertions.assertTrue(result1.success());
//        Result result2 = new AriusClient().get(RequestPathOP.CONFIG_LIST);
//    }
//
//    /**
//     * body当中是只有一个对应的id值
//     * 1.
//     */
//    @Test
//    public void deleteConfig() throws IOException {
//        Result result2 = new AriusClient().get(RequestPathOP.CONFIG_LIST);
//        result2.getData();
//        AriusConfigInfoDTO ariusConfigInfoDTO = AriusDataSource.ariusConfigInfoDTOFactory();
//        //Result result = new AriusClient().delete();
//        //Assertions.assertTrue(result.success());
//    }
//
//    @Test
//    public void editConfig() throws IOException {
//        AriusConfigInfoDTO ariusConfigInfoDTO= AriusDataSource.ariusConfigInfoDTOFactory();
//        Result result = new AriusClient().post(RequestPathOP.CONFIG_EDIT, ariusConfigInfoDTO);
//        Assertions.assertTrue(result.success());
//    }
//
//    /**
//     * id和status(禁用的时候表示为2，开启的时候表示为1)
//     */
//    @Test
//    public void switchConfig() {
//
//    }
//}
