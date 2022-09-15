package com.didichuxing.datachannel.arius.admin.core.service.metrics.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.UserConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.config.UserConfigInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.UserConfigPO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.UserConfigTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.UserConfigService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.metrics.UserConfigDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangpengkai
 * @date 2021/08/10
 */
@Service
public class UserConfigServiceImpl implements UserConfigService {

    private static final ILog    LOGGER    = LogFactory.getLog(UserConfigServiceImpl.class);

    private static final String  USER_NAME = "user_name";

    private static final String  PROJECT_ID = "project_id";

    private static final String CONFIG_TYPE = "config_type";

    @Autowired
    private UserConfigDAO userConfigDAO;

    @Override
    public List<String> getMetricsByTypeAndUserName(UserConfigInfoDTO param) {
        UserConfigTypeEnum userConfigTypeEnum = userConfigDTO2Type(param);

        Result<Void> result = paramCheck(param.getUserName(),param.getProjectId(), userConfigTypeEnum);
        if (result.failed()) {
            return new ArrayList<>();
        }
        UserConfigPO userConfigPO = getUserConfigByDomainAccount(param);
        if (null == userConfigPO || AriusObjUtils.isNull(userConfigPO.getConfigInfo())) {
            return new ArrayList<>();
        }

        List<UserConfigInfo> userConfigInfos = JSON.parseArray(userConfigPO.getConfigInfo(),
            UserConfigInfo.class);

        // 获取对应属性下的配置列表
        List<List<String>> metricsList = userConfigInfos.stream()
            .filter(userConfigInfoPO -> userConfigTypeMatch(userConfigInfoPO, userConfigTypeEnum))
            .map(UserConfigInfo::getUserConfigTypes).collect(Collectors.toList());

        if (metricsList.isEmpty()) {
            LOGGER.info("对应属性的配置内容尚未入库");
            return new ArrayList<>();
        }
        return metricsList.get(0);
    }

    @Override
    public Result<Integer> updateByMetricsByTypeAndUserName(UserConfigInfoDTO param) {
        UserConfigTypeEnum userConfigTypeEnum = userConfigDTO2Type(param);

        Result<Void> result = paramCheck(param.getUserName(), param.getProjectId(), userConfigTypeEnum);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        UserConfigPO userConfigPO = getUserConfigByDomainAccount(param);
        if (null == userConfigPO) {
            return insertUserConfigInfoWithoutCheck(param);
        }

        List<UserConfigInfo> userConfigInfos = JSON.parseArray(userConfigPO.getConfigInfo(),
            UserConfigInfo.class);

        boolean ifPresent = false;
        for (UserConfigInfo userConfigInfoPO : userConfigInfos) {
            if (userConfigTypeMatch(userConfigInfoPO, userConfigTypeEnum)) {
                userConfigInfoPO.setUserConfigTypes(param.getUserConfigTypes());
                ifPresent = true;
                break;
            }
        }

        if (BooleanUtils.isFalse(ifPresent)) {
            userConfigInfos.add(createUserConfigInfoWithoutCheck(param));
        }

        userConfigPO.setConfigInfo(JSON.toJSONString(userConfigInfos));
        boolean succ = (1 == userConfigDAO.update(userConfigPO,buildQueryWrapper(param)));
        return Result.build(succ, userConfigPO.getId());
    }

    @Override
    public void deleteByUserName(String userName,Integer projectId) {
        userConfigDAO.delete(new QueryWrapper<UserConfigPO>().eq(USER_NAME, userName).eq(PROJECT_ID,projectId));
    }

    /**************************************************** private methods ****************************************************/

    private UserConfigTypeEnum userConfigDTO2Type(UserConfigInfoDTO param) {
        if (null == param) {
            return UserConfigTypeEnum.UNKNOWN;
        }
        for (UserConfigTypeEnum typeEnum : UserConfigTypeEnum.values()) {
            if (typeEnum.getFirstUserConfigType().equals(param.getFirstUserConfigType())
                && typeEnum.getSecondUserConfigType().equals(param.getSecondUserConfigType())) {
                return typeEnum;
            }
        }
        return UserConfigTypeEnum.UNKNOWN;
    }

    private Result<Void> paramCheck(String userName, Integer projectId, UserConfigTypeEnum userConfigTypeEnum) {
        if (AriusObjUtils.isNull(userName)) {
            return Result.buildFail("用户账号为空");
        }
        if(AriusObjUtils.isNull(projectId)){
            return Result.buildFail("应用ID为空");
        }
        if (userConfigTypeEnum.getCode() == UserConfigTypeEnum.UNKNOWN.getCode()) {
            return Result.buildFail("配置类型未知");
        }

        return Result.buildSucc();
    }

    private UserConfigPO getUserConfigByDomainAccount(UserConfigInfoDTO userConfigInfoDTO) {
        QueryWrapper<UserConfigPO> queryWrapper = buildQueryWrapper(userConfigInfoDTO);
        return userConfigDAO.selectOne(queryWrapper);
    }

    private boolean userConfigTypeMatch(UserConfigInfo userConfigInfoPO, UserConfigTypeEnum userConfigTypeEnum) {
        return userConfigInfoPO.getFirstUserConfigType().equals(userConfigTypeEnum.getFirstUserConfigType())
               && userConfigInfoPO.getSecondUserConfigType().equals(userConfigTypeEnum.getSecondUserConfigType());
    }

    private Result<Integer> insertUserConfigInfoWithoutCheck(UserConfigInfoDTO param) {
        UserConfigPO userConfigPO = new UserConfigPO();
        userConfigPO.setUserName(param.getUserName());
        userConfigPO.setProjectId(param.getProjectId());
        userConfigPO.setConfigType(param.getConfigType());
        userConfigPO.setConfigInfo(JSON.toJSONString(Arrays.asList(createUserConfigInfoWithoutCheck(param))));
        boolean succ = (1 == userConfigDAO.insert(userConfigPO));
        return Result.build(succ, userConfigPO.getId());
    }

    private UserConfigInfo createUserConfigInfoWithoutCheck(UserConfigInfoDTO param) {
        UserConfigInfo userConfigInfo = new UserConfigInfo();
        userConfigInfo.setUserConfigTypes(param.getUserConfigTypes());
        userConfigInfo.setFirstUserConfigType(param.getFirstUserConfigType());
        userConfigInfo.setSecondUserConfigType(param.getSecondUserConfigType());
        userConfigInfo.setUserName(param.getUserName());
        userConfigInfo.setProjectId(param.getProjectId());
        return userConfigInfo;
    }
    /**
     * 构建querywrapper
     * @param param
     * @return
     */
    private QueryWrapper<UserConfigPO> buildQueryWrapper(UserConfigInfoDTO param) {
        QueryWrapper<UserConfigPO> userConfigPOQueryWrapper = new QueryWrapper<>();
        userConfigPOQueryWrapper.eq(USER_NAME, param.getUserName());
        userConfigPOQueryWrapper.eq(PROJECT_ID,param.getProjectId());
        userConfigPOQueryWrapper.eq(CONFIG_TYPE, param.getConfigType());
        return userConfigPOQueryWrapper;
    }
}