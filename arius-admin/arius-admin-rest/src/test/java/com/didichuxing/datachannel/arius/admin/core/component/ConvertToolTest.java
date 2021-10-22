package com.didichuxing.datachannel.arius.admin.core.component;

import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppPO;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;

/**
 * @author d06679
 * @date 2019/4/18
 */
public class ConvertToolTest extends AriusAdminApplicationTests {
    @Test
    public void list2String() throws Exception {

    }

    @Test
    public void list2List() throws Exception {

    }

    @Test
    public void obj2Obj() throws Exception {
        AppPO appPO = new AppPO();
        appPO.setResponsible("69");

        App app = ConvertUtil.obj2Obj(appPO, App.class);
        Assert.assertTrue(!app.getResponsible().equals("69"));
    }
}