package com.didichuxing.datachannel.arius.admin.method.v3.normal;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wuxuan
 * @Date 2022/3/31
 */
public class NormalAccountControllerMethod {

    public static final String Accout = V3_NORMAL + "/account";
    //
    //public static Result<List<AriusUserInfoVO>> searchOnJobStaffByKeyWord(String keyWord) throws IOException{
    //    String path=String.format("%s/search", Accout);
    //    Map<String, Object> params = new HashMap<>();
    //    params.put("keyWord", keyWord);
    //    return JSON.parseObject(AriusClient.get(path,params), new TypeReference<Result<List<AriusUserInfoVO>>>(){});
    //}
    //
    //public static Result<AriusUserInfoVO> role() throws IOException{
    //    String path=String.format("%s/role",Accout);
    //    return JSON.parseObject(AriusClient.get(path),new TypeReference<Result<AriusUserInfoVO>>(){});
    //}

}