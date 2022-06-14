package com.didichuxing.datachannel.arius.admin.core.service.metrics.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.config.MetricsConfigInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.UserMetricsConfigPO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.UserMetricsConfigService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.metrics.UserMetricsConfigDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author wangpengkai
 * @date 2021/08/10
 */
@Service
public class MetricsConfigServiceImpl implements UserMetricsConfigService {

    private static final ILog LOGGER = LogFactory.getLog(MetricsConfigServiceImpl.class);

    private static final String USER_NAME = "user_name";

    @Autowired
    private UserMetricsConfigDAO userMetricsConfigDAO;

    @Override
    public List<String> getMetricsByTypeAndUserName(MetricsConfigInfoDTO param) {
        MetricsTypeEnum metricsTypeEnum = metricsConfigDTO2Type(param);

        Result<Void> result = paramCheck(param.getUserName(), metricsTypeEnum);
        if (result.failed()) {
            return new ArrayList<>();
        }

        UserMetricsConfigPO userMetricsConfigPO = getMetricsInfoByDomainAccount(param.getUserName());
        if (null == userMetricsConfigPO || AriusObjUtils.isNull(userMetricsConfigPO.getMetricInfo())) {
            return new ArrayList<>();
        }

        List<MetricsConfigInfo> metricsConfigInfos = JSON.parseArray(userMetricsConfigPO.getMetricInfo(),
                MetricsConfigInfo.class);

        // 获取对应属性下的配置列表
        List<List<String>> metricsList = metricsConfigInfos
                .stream()
                .filter(metricsConfigInfoPO -> metricsTypeMatch(metricsConfigInfoPO, metricsTypeEnum))
                .map(MetricsConfigInfo::getMetricsTypes)
                .collect(Collectors.toList());

        if (metricsList.isEmpty()) {
            LOGGER.info("对应属性的配置内容尚未入库");
            return new ArrayList<>();
        }
        return metricsList.get(0);
    }

    @Override
    public Result<Integer> updateByMetricsByTypeAndUserName(MetricsConfigInfoDTO param) {
        MetricsTypeEnum metricsTypeEnum = metricsConfigDTO2Type(param);

        Result<Void> result = paramCheck(param.getUserName(), metricsTypeEnum);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        UserMetricsConfigPO userMetricsConfigPO = getMetricsInfoByDomainAccount(param.getUserName());
        if (null == userMetricsConfigPO) {
            return insertMetricsConfigInfoWithoutCheck(param);
        }

        List<MetricsConfigInfo> metricsConfigInfos = JSON.parseArray(userMetricsConfigPO.getMetricInfo(),
                MetricsConfigInfo.class);

        boolean ifPresent = false;
        for (MetricsConfigInfo metricsConfigInfoPO : metricsConfigInfos) {
            if (metricsTypeMatch(metricsConfigInfoPO, metricsTypeEnum)) {
                metricsConfigInfoPO.setMetricsTypes(param.getMetricsTypes());
                ifPresent = true;
                break;
            }
        }

        if (BooleanUtils.isFalse(ifPresent)) {
            metricsConfigInfos.add(createMetricsConfigInfoWithoutCheck(param));
        }

        userMetricsConfigPO.setMetricInfo(JSON.toJSONString(metricsConfigInfos));
        boolean succ = (1 == userMetricsConfigDAO.update(
                userMetricsConfigPO, new QueryWrapper<UserMetricsConfigPO>().eq(USER_NAME, param.getUserName())));
        return Result.build(succ, userMetricsConfigPO.getId());
    }

    @Override
    public void deleteByUserName(String userName) {
        userMetricsConfigDAO.delete(new QueryWrapper<UserMetricsConfigPO>().eq(USER_NAME, userName));
    }

    /**************************************************** private methods ****************************************************/

    private MetricsTypeEnum metricsConfigDTO2Type(MetricsConfigInfoDTO param) {
        if (null == param) {
            return MetricsTypeEnum.UNKNOWN;
        }
        for (MetricsTypeEnum typeEnum : MetricsTypeEnum.values()) {
            if (typeEnum.getFirstMetricsType().equals(param.getFirstMetricsType())
                    && typeEnum.getSecondMetricsType().equals(param.getSecondMetricsType())) {
                return typeEnum;
            }
        }
        return MetricsTypeEnum.UNKNOWN;
    }

    private Result<Void> paramCheck(String userName, MetricsTypeEnum metricsTypeEnum) {
        if (AriusObjUtils.isNull(userName)) {
            return Result.buildFail("用户账号为空");
        }

        if (metricsTypeEnum.getCode() == MetricsTypeEnum.UNKNOWN.getCode()) {
            return Result.buildFail("指标看板未知");
        }

        return Result.buildSucc();
    }

    private UserMetricsConfigPO getMetricsInfoByDomainAccount(String userName) {
        return userMetricsConfigDAO.selectOne(new QueryWrapper<UserMetricsConfigPO>().eq(USER_NAME, userName));
    }

    private boolean metricsTypeMatch(MetricsConfigInfo metricsConfigInfoPO, MetricsTypeEnum metricsTypeEnum) {
        return metricsConfigInfoPO.getFirstMetricsType().equals(metricsTypeEnum.getFirstMetricsType())
               && metricsConfigInfoPO.getSecondMetricsType().equals(metricsTypeEnum.getSecondMetricsType());
    }

    private Result<Integer> insertMetricsConfigInfoWithoutCheck(MetricsConfigInfoDTO param) {
        UserMetricsConfigPO userMetricsConfigPO = new UserMetricsConfigPO();
        userMetricsConfigPO.setUserName(param.getUserName());
        userMetricsConfigPO.setMetricInfo(JSON.toJSONString(Arrays.asList(createMetricsConfigInfoWithoutCheck(param))));
        boolean succ = (1 == userMetricsConfigDAO.insert(userMetricsConfigPO));
        return Result.build(succ, userMetricsConfigPO.getId());
    }

    private MetricsConfigInfo createMetricsConfigInfoWithoutCheck(MetricsConfigInfoDTO param) {
        MetricsConfigInfo metricsConfigInfo = new MetricsConfigInfo();
        metricsConfigInfo.setMetricsTypes(param.getMetricsTypes());
        metricsConfigInfo.setFirstMetricsType(param.getFirstMetricsType());
        metricsConfigInfo.setSecondMetricsType(param.getSecondMetricsType());
        metricsConfigInfo.setUserName(param.getUserName());
        return metricsConfigInfo;
    }
}