package com.didichuxing.datachannel.arius.admin.v3.normal;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.user.AriusUserInfoVO;
import com.didichuxing.datachannel.arius.admin.method.v3.normal.NormalAccountControllerMethod;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author wuxuan
 * @Date 2022/3/31
 */
public class NormalAccountTests extends BaseContextTest {

    @Test
    public void testSearchOnJobStaffByKeyWord() throws IOException {
        Result<List<AriusUserInfoVO>> result= NormalAccountControllerMethod.searchOnJobStaffByKeyWord("admin");
        Assert.assertTrue(result.success());
    }

    @Test
    public void testRole() throws IOException{
        Result<AriusUserInfoVO> result=NormalAccountControllerMethod.role();
        Assert.assertTrue(result.success());
    }
}
