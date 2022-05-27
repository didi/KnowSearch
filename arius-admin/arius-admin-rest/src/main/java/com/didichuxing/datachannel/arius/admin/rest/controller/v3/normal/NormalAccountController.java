package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.user.AriusUserInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Deprecated
@RequestMapping(V3_NORMAL + "/account")
@Api(tags = "Normal-Account相关接口(REST)")
public class NormalAccountController {

    @Autowired
    private AriusUserInfoService        ariusUserInfoService;

    @GetMapping("/search")
    @ResponseBody
    @ApiOperation(value = "账号搜索", notes = "仅支持搜索, 不支持全部展示")
    public Result<List<AriusUserInfoVO>> searchOnJobStaffByKeyWord(@RequestParam("keyWord") String keyWord) {
        List<AriusUserInfo> ariusUserInfos = ariusUserInfoService.searchOnJobStaffByKeyWord(keyWord);
        if(CollectionUtils.isEmpty(ariusUserInfos)){
            return Result.buildSuccWithMsg("查询为空");
        }

        return Result.buildSucc(ConvertUtil.list2List(ariusUserInfos, AriusUserInfoVO.class));
    }


    @GetMapping("/role")
    @ResponseBody
    @ApiOperation(value = "查询角色", notes = "查询角色的权限")
    public Result<AriusUserInfoVO> role(HttpServletRequest request) {
        String username = HttpRequestUtil.getOperator(request);

        AriusUserInfo ariusUserInfo = ariusUserInfoService.getByDomainAccount(username);
        if(null == ariusUserInfo){
            // 用户角色信息不存在的情况下, 默认为Normal
            ariusUserInfo = new AriusUserInfo();
            ariusUserInfo.setDomainAccount(username);
            ariusUserInfo.setName(username);
            ariusUserInfo.setRole(AriusUserRoleEnum.NORMAL.getRole());
        }

        return Result.buildSucc(ConvertUtil.obj2Obj(ariusUserInfo, AriusUserInfoVO.class));
    }
}