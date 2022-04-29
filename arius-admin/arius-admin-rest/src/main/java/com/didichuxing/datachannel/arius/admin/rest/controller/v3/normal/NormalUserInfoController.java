package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.user.EditUserPasswordDTO;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.biz.user.AriusUserInfoManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.user.AriusUserInfoVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Created by linyunan on 2021-06-22
 */
@RestController
@RequestMapping(V3_NORMAL + "/user")
@Api(tags = "用户信息接口(REST)")
public class NormalUserInfoController {

    @Autowired
    private AriusUserInfoManager ariusUserInfoManager;

    @GetMapping("list")
    @ResponseBody
    @ApiOperation(value = "获取用户列表")
    public Result<List<AriusUserInfoVO>> listUserVOS() {
        return Result.buildSucc(ariusUserInfoManager.listUserVOS());
    }

    @GetMapping("{userName}/check")
    @ResponseBody
    @ApiOperation(value = "用户名称是否合法")
    public Result<Void> checkUserNameValid(@PathVariable String userName) {
        return ariusUserInfoManager.checkUserNameValid(userName);
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "编辑用户信息",notes = "不包含密码")
    public Result<Void> editUsersInfoVO(@RequestBody AriusUserInfoDTO ariusUserInfoDTO) {
        return ariusUserInfoManager.editUsersInfoVO(ariusUserInfoDTO);
    }

    @PutMapping("password")
    @ResponseBody
    @ApiOperation(value = "编辑用户密码")
    public Result<Void> editUsersPassword(@RequestBody EditUserPasswordDTO editUserPasswordDTO) {
        return ariusUserInfoManager.editUsersPassword(editUserPasswordDTO);
    }

    @GetMapping("{domainAccount}/get")
    @ResponseBody
    @ApiOperation(value = "获取单个用户信息")
    public Result<AriusUserInfoVO> getAriusUserInfoVO(@PathVariable String domainAccount) {
        return ariusUserInfoManager.getAriusUserInfoVO(domainAccount);
    }
}
