package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import com.didichuxing.datachannel.arius.admin.biz.app.AppManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ConsoleAppDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ConsoleAppLoginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppWithVerifyCodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.metadata.service.GatewayJoinLogService;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Component
public class AppManagerImpl implements AppManager {

    private static final String GET_USER_APPID_LIST_TICKET = "xTc59aY72";
    private static final String GET_USER_APPID_LIST_TICKET_NAME = "X-ARIUS-APP-TICKET";

    @Autowired
    private AppService appService;

    @Autowired
    private GatewayJoinLogService gatewayJoinLogService;

    /**
     * 登陆APP接口
     */
    @Override
    public Result<Void> login(HttpServletRequest request, ConsoleAppLoginDTO loginDTO) {
        return appService.login(loginDTO.getAppId(), loginDTO.getVerifyCode(), HttpRequestUtils.getOperator(request));
    }

    /**
     * 查询用户可以免密登陆的APP接口,该接口包含APP的校验码等敏感信息,需要调用方提供ticket
     */
    @Override
    public Result<List<ConsoleAppWithVerifyCodeVO>> getNoCodeLogin(HttpServletRequest request,
                                                                   @RequestParam("user") String user) {
        String ticket = request.getHeader(GET_USER_APPID_LIST_TICKET_NAME);
        if (!GET_USER_APPID_LIST_TICKET.equals(ticket)) {
            return Result.buildParamIllegal("ticket错误");
        }

        return Result.buildSucc(
                ConvertUtil.list2List(appService.getUserLoginWithoutCodeApps(user), ConsoleAppWithVerifyCodeVO.class));
    }

    @Override
    public Result<List<String>> listNames() {
        return Result.buildSucc(appService.listApps().parallelStream().map(App::getName).collect(Collectors.toList()));
    }

    @Override
    public Result<List<String>> listIds() {
        return Result.buildSucc(appService.listApps().parallelStream().map(App::getId).map(String::valueOf).collect(Collectors.toList()));
    }

    /**
     * APP列表接口
     */
    @Override
    public Result<List<ConsoleAppVO>> list() {
        return Result.buildSucc(ConvertUtil.list2List(appService.listApps(), ConsoleAppVO.class));
    }

    /**
     * 编辑APP接口
     */
    @Override
    public Result<Void> update(HttpServletRequest request, ConsoleAppDTO appDTO) {
        return appService.editApp(ConvertUtil.obj2Obj(appDTO, AppDTO.class), HttpRequestUtils.getOperator(request));
    }

    /**
     * 获取APP详情接口
     */
    @Override
    public Result<ConsoleAppVO> get(Integer appId) {
        return Result.buildSucc(ConvertUtil.obj2Obj(appService.getAppById(appId), ConsoleAppVO.class));
    }

    /**
     * 获取访问次数接口
     */
    @Override
    public Result<Long> accessCount(Integer appId) {
        App app = appService.getAppById(appId);
        if (app == null) {
            return Result.buildNotExist("应用不存在");
        }

        Date nowDate = new Date();
        Date accessStartTime = AriusDateUtils.getBeforeDays(nowDate, 30);
        return gatewayJoinLogService.getSearchCountByAppid((long) app.getId(), accessStartTime.getTime(),
                nowDate.getTime());
    }

    /**
     * 删除APP
     */
    @Override
    public Result<Void> delete(HttpServletRequest request, @RequestParam("appId") Integer appId) {
        return appService.deleteAppById(appId, HttpRequestUtils.getOperator(request));
    }
}
