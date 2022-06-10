package com.didichuxing.datachannel.arius.admin.method.v3.thirdpart;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

import java.io.IOException;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_THIRD_PART_SSO;

/**
 * @author wuxuan
 * @Date 2022/3/30
 */
public class LoginControllerMethod {
    public static final String SSO_Login = V3_THIRD_PART_SSO;

    //public static Result<Boolean> teslLogin(LoginDTO loginDTO) throws IOException{
    //    String path = String.format("%s/login", SSO_Login);
    //    return JSON.parseObject(AriusClient.post(path, loginDTO), new TypeReference<Result<Boolean>>(){});
    //}

    public static Result<Boolean> testLogout() throws IOException{
        String path=String.format("%s/logout",SSO_Login);
        return JSON.parseObject(AriusClient.delete(path),new TypeReference<Result<Boolean>>(){});
    }

    //public static Result<Long> testRegister(AriusUserInfoDTO userInfoDTO) throws IOException{
    //    String path=String.format("%s/register",SSO_Login);
    //    return JSON.parseObject(AriusClient.post(path,userInfoDTO),new TypeReference<Result<Long>>(){});
    //}

    public static Result<String> testGetPublicKey() throws IOException{
        String path=String.format("%s/publicKey",SSO_Login);
        return JSON.parseObject(AriusClient.get(path),new TypeReference<Result<String>>(){});
    }
}