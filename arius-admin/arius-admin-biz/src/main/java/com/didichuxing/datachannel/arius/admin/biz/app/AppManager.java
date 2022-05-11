package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ConsoleAppDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ConsoleAppLoginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppWithVerifyCodeVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
/**
 * @author linyunan
 * @date 2021-04-28
 */
public interface AppManager {
    /**
     * 登陆APP接口
     * @param request     request
     * @param loginDTO    loginDTO
     * @return            Result<Void>
     */
    Result<Void> login(HttpServletRequest request, ConsoleAppLoginDTO loginDTO);

    /**
     * 查询用户可以免密登陆的APP接口,该接口包含APP的校验码等敏感信息,需要调用方提供ticket
     * @param request   request
     * @param user      用户名
     * @return          Result<List<ConsoleAppWithVerifyCodeVO>>
     */
    Result<List<ConsoleAppWithVerifyCodeVO>> getNoCodeLogin(HttpServletRequest request, String user);

    /**
     * APP列表接口
     * @return     Result<List<ConsoleAppVO>>
     */
    Result<List<ConsoleAppVO>> list();

    /**
     * APP列表接口
     * @return     Result<List<String>>
     */
    Result<List<String>> listIds();

    /**
     * 编辑APP接口
     * @param request    request
     * @param appDTO     appDTO
     * @return           Result<Void>
     */
    Result<Void> update(HttpServletRequest request, ConsoleAppDTO appDTO);

    /**
     * 获取APP详情接口
     * @param appId  appId
     * @return       Result<ConsoleAppVO>
     */
    Result<ConsoleAppVO> get(Integer appId);

    /**
     * 获取访问次数接口
     * @param appId appId
     * @return     登录次数
     */
    Result<Long> accessCount(Integer appId);

    /**
     * 删除APP
     * @param request   request
     * @param appId     appId
     * @return          Result<Void>
     */
    Result<Void> delete(HttpServletRequest request, Integer appId);
}
