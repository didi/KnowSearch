package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.ConsoleAppDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.ConsoleAppLoginDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.ConsoleAppVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.ConsoleAppWithVerifyCodeVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface AppManager {

    /**
     * 登陆APP接口
     */
    Result login(HttpServletRequest request, ConsoleAppLoginDTO loginDTO);

    /**
     * 查询用户可以免密登陆的APP接口,该接口包含APP的校验码等敏感信息,需要调用方提供ticket
     */
    Result<List<ConsoleAppWithVerifyCodeVO>> getNoCodeLogin(HttpServletRequest request, String user);

    /**
     * APP列表接口
     */
    Result<List<ConsoleAppVO>> list();

    /**
     * 编辑APP接口
     */
    Result update(HttpServletRequest request, ConsoleAppDTO appDTO);

    /**
     * 获取APP详情接口
     */
    Result<ConsoleAppVO> get(Integer appId);

    /**
     * 获取访问次数接口
     */
    Result update(Integer appId);

    /**
     * 删除APP
     */
    Result update(HttpServletRequest request, Integer appId);
}
