package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.user.AriusUserInfoVO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType.ILLEGAL_PARAMS;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author zengqiao
 * @date 19/5/3
 */
@Api(tags = "OP-Account相关接口(REST)")
@RestController
@RequestMapping(V3_OP + "/account")
public class OpAccountController {
    private final static Logger  logger = LoggerFactory.getLogger(OpAccountController.class);

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "添加账号", notes = "")
    public Result<Boolean> addAccount(@RequestBody AriusUserInfoDTO dto) {
        if (!dto.legal()) {
            return Result.build(ILLEGAL_PARAMS);
        }

        AriusUserInfo ariusUserInfo = ariusUserInfoService.getByName(dto.getName());
        if (ariusUserInfo == null || AriusUserStatusEnum.DISABLE.getCode() == ariusUserInfo.getStatus()) {
            // 用户不存在 or 处于逻辑删除的状态, 则直接添加
            return Result.buildSucc(ariusUserInfoService.addUserRole(dto));
        }

        // 该用户是OP | RD , 则返回用户已经存在
        if(ariusUserInfo.getRole().equals(AriusUserRoleEnum.OP.getRole())
                || ariusUserInfo.getRole().equals(AriusUserRoleEnum.RD.getRole())) {
            String msg = "该用户已存在" + AriusUserRoleEnum.getUserRoleEnum(ariusUserInfo.getRole()).getDesc() + "，如需修改，请点击修改按钮!";
            return Result.buildFail(msg);
        }

        // 用户非RD及OP, 则直接增加该用户
        return Result.buildSucc(ariusUserInfoService.addUserRole(dto));
    }

    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "删除账号", notes = "")
    public Result<Boolean> deleteAccount(@RequestParam("username") String username) {
        return Result.buildSucc(ariusUserInfoService.deleteUserRole(username));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "修改账号", notes = "")
    public Result<Boolean> updateAccount(@RequestBody AriusUserInfoDTO dto) {
        if (!dto.legal()) {
            return Result.build(ILLEGAL_PARAMS);
        }

        return Result.buildSucc(ariusUserInfoService.updateUserRole(dto));
    }

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "账号列表", notes = "")
    public Result<List<AriusUserInfoVO>> listAccounts() {
        try {
            List<AriusUserInfo> ariusUserInfos = ariusUserInfoService.listByRoles(Arrays.asList(AriusUserRoleEnum.NORMAL.getRole(),
                    AriusUserRoleEnum.OP.getRole(), AriusUserRoleEnum.RD.getRole()));
            return Result.buildSucc(ConvertUtil.list2List(ariusUserInfos, AriusUserInfoVO.class));
        } catch (Exception e) {
            logger.error("listAccounts@AdminAccountController, list failed.", e);
            return Result.buildFail(e.getMessage());
        }
    }
}
