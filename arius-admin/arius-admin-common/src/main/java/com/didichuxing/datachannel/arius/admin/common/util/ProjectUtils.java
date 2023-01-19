package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.common.vo.project.ProjectVO;
import com.didiglobal.knowframework.security.common.vo.user.UserBriefVO;

public final class ProjectUtils {
    private static final ILog LOGGER = LogFactory.getLog(CommonUtils.class);

    private ProjectUtils() {
    }

    /**
    * 检查项目是否匹配当前业务的操作
    *
    * @param func                     函数
    * @param r                         操作点业务标识 各个业务的点
    * @param projectId                项目id 接口传入的项目id
    * @return {@code Boolean}
    */
    public static <R> Result<Void> checkProjectCorrectly(Function<R, Integer> func, R r, Integer projectId) {
        final int currentProjectId = func.apply(r);
        if (AuthConstant.SUPER_PROJECT_ID.equals(currentProjectId)) {
            return Result.buildSucc();
        }
        if (Objects.equals(currentProjectId, projectId)) {
            return Result.buildSucc();
        }
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            return Result.buildSucc();
        }
        return Result.buildFail("当前项目不属于超级项目或者持有该操作的项目");
    }

    /**
    * 用户名是属于项目成员
    * @see ProjectVO#getUserList()
    * @param userName 用户名
    * @param projectVO
    * @return boolean
    */
    public static boolean isUserNameBelongProjectMember(String userName, ProjectVO projectVO) {
        return StringUtils.hasText(userName)
               && Optional.ofNullable(projectVO).map(ProjectVO::getUserList).orElse(Collections.emptyList()).stream()
                   .map(UserBriefVO::getUserName).anyMatch(username -> username.equals(userName));
    }

    /**
     * 判断用户名属于项目拥有者
     * @see  ProjectVO#getOwnerList()
     * @param userName 用户名
     * @param projectVO
     * @return boolean
     */
    public static boolean isUserNameBelongProjectResponsible(String userName, ProjectVO projectVO) {
        return StringUtils.hasText(userName)
               && Optional.ofNullable(projectVO).map(ProjectVO::getOwnerList).orElse(Collections.emptyList()).stream()
                   .map(UserBriefVO::getUserName).anyMatch(username -> username.equals(userName));
    }

    /**
     * 获取变更之前和变更之后的json
     *
     * @param afterObj  obj后
     * @param beforeObj obj之前
     * @return {@code String}
     */
    public static String getChangeByAfterAndBeforeJson(Object afterObj, Object beforeObj) {
        return new JSONObject().fluentPut("beforeChange", beforeObj).fluentPut("afterChange", afterObj).toJSONString();
    }
}