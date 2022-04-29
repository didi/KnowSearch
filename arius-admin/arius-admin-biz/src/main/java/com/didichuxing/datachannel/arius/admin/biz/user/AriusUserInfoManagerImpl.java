package com.didichuxing.datachannel.arius.admin.biz.user;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.user.EditUserPasswordDTO;
import com.didichuxing.datachannel.arius.admin.common.component.RSATool;
import java.util.List;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.user.AriusUserInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;

/**
 * Created by linyunan on 2021-06-22
 */
@Component
public class AriusUserInfoManagerImpl implements AriusUserInfoManager {

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    @Autowired
    private RSATool              rsaTool;

    private static final int     USER_NAME_MAX_LENGTH = 50;

    @Override
    public Result<Void> checkUserNameValid(String userName) {
        if (userName.length() > USER_NAME_MAX_LENGTH) {
            return Result.buildFail(String.format("用户名称长度超过%s", USER_NAME_MAX_LENGTH));
        }

        if (ariusUserInfoService.isExist(userName)) {
            return Result.buildFail("用户名称重复, 请重新输入");
        }
        return Result.buildSucc();
    }

    @Override
    public List<AriusUserInfoVO> listUserVOS() {
        return ConvertUtil.list2List(ariusUserInfoService.listAllEnable(), AriusUserInfoVO.class);
    }

    @Override
    public Result<Void> editUsersPassword(EditUserPasswordDTO editUserPasswordDTO) {
        if (AriusObjUtils.isNull(editUserPasswordDTO)) {
            return Result.buildParamIllegal("参数为空");
        }

        AriusUserInfo ariusUserInfo = ariusUserInfoService.getByDomainAccount(editUserPasswordDTO.getDomainAccount());
        if (AriusObjUtils.isNull(ariusUserInfo)) {
            return Result.buildParamIllegal(String.format("账号%s不存在", editUserPasswordDTO.getDomainAccount()));
        }

        //解码
        Result<String> oldPassWordResult = rsaTool.decrypt(editUserPasswordDTO.getOldPassWord());
        if (oldPassWordResult.failed()) {
            return Result.buildFrom(oldPassWordResult);
        }
        Result<String> newPassWordResult = rsaTool.decrypt(editUserPasswordDTO.getNewPassWord());
        if (newPassWordResult.failed()) {
            return Result.buildFrom(newPassWordResult);
        }

        String oldPassWord = oldPassWordResult.getData();
        String newPassWord = newPassWordResult.getData();

        if (!Objects.equals(ariusUserInfo.getPassword(), oldPassWord)) {
            return Result.buildFail("请确认旧密码是否正确");
        }

        ariusUserInfo.setPassword(newPassWord);
        Boolean flag = ariusUserInfoService.updateUserInfo(ConvertUtil.obj2Obj(ariusUserInfo, AriusUserInfoDTO.class));
        if (Boolean.FALSE.equals(flag)) {
            return Result.buildFail();
        }

        return Result.buildSucc();
    }

    @Override
    public Result<Void> editUsersInfoVO(AriusUserInfoDTO ariusUserInfoDTO) {
        if (AriusObjUtils.isNull(ariusUserInfoDTO)) {
            return Result.buildParamIllegal("参数为空");
        }

        AriusUserInfo oldAriusUser = ariusUserInfoService.getByDomainAccount(ariusUserInfoDTO.getDomainAccount());
        if (AriusObjUtils.isNull(oldAriusUser)) {
            return Result.buildParamIllegal(String.format("账号%s不存在", ariusUserInfoDTO.getDomainAccount()));
        }

        ariusUserInfoDTO.setPassword(null);
        ariusUserInfoDTO.setId(oldAriusUser.getId());
        Boolean flag = ariusUserInfoService.updateUserInfo(ariusUserInfoDTO);
        if (Boolean.FALSE.equals(flag)) {
            return Result.buildFail();
        }

        return Result.buildSucc();
    }

    @Override
    public Result<AriusUserInfoVO> getAriusUserInfoVO(String domainAccount) {
        AriusUserInfoVO ariusUserInfoVO = ConvertUtil.obj2Obj(ariusUserInfoService.getByDomainAccount(domainAccount),
            AriusUserInfoVO.class);

        return Result.buildSucc(ariusUserInfoVO);
    }
}
