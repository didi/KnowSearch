package com.didichuxing.datachannel.arius.admin.v3.normal;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.method.v3.normal.NormalDepartControllerMethod;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author wuxuan
 * @Date 2022/3/31
 */
public class NormalDepartTests extends BaseContextTest {

    @Test
    public void testListDepartments() throws IOException{
        Result<String> result= NormalDepartControllerMethod.listDepartments();
        Assert.assertTrue(result.success());
    }
}
