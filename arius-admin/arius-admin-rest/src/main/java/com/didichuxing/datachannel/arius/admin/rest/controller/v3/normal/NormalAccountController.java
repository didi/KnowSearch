package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.user.AriusUserInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserLoginRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

@RestController
@RequestMapping(V3_NORMAL + "/account")
@Api(tags = "Normal-Account相关接口(REST)")
public class NormalAccountController {

    @Autowired
    private AriusUserInfoService        ariusUserInfoService;

    @Autowired
    private AriusUserLoginRecordService userLoginRecordService;

    @GetMapping("/search")
    @ResponseBody
    @ApiOperation(value = "账号搜索", notes = "仅支持搜索, 不支持全部展示")
    public Result<List<AriusUserInfoVO>> searchOnJobStaffByKeyWord(@RequestParam("keyWord") String keyWord) {
        List<AriusUserInfo> ariusUserInfos = ariusUserInfoService.searchOnJobStaffByKeyWord(keyWord);
        if(CollectionUtils.isEmpty(ariusUserInfos)){
            return Result.buildSucc("查询为空");
        }

        return Result.buildSucc(ConvertUtil.list2List(ariusUserInfos, AriusUserInfoVO.class));
    }


    @GetMapping("/role")
    @ResponseBody
    @ApiOperation(value = "查询角色", notes = "查询角色的权限")
    public Result<AriusUserInfoVO> role(HttpServletRequest request) {
        String username = HttpRequestUtils.getOperator(request);

        AriusUserInfo ariusUserInfo = ariusUserInfoService.getByName(username);
        if(null == ariusUserInfo){
            // 用户角色信息不存在的情况下, 默认为Normal
            ariusUserInfo = new AriusUserInfo();
            ariusUserInfo.setDomainAccount(username);
            ariusUserInfo.setName(username);
            ariusUserInfo.setRole(AriusUserRoleEnum.NORMAL.getRole());
        }

        return Result.buildSucc(ConvertUtil.obj2Obj(ariusUserInfo, AriusUserInfoVO.class));
    }


    @GetMapping("/isGuide")
    @ResponseBody
    @ApiOperation(value = "是否提供向导", notes = "是否提供向导")
    public Result<Boolean> isGuide(HttpServletRequest request) {
        String username = HttpRequestUtils.getOperator(request);
        return Result.buildSucc(userLoginRecordService.isFirstLogin(username));
    }

}
