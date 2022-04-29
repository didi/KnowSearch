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
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.MetricsConfigPO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.metrics.MetricsConfigService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.metrics.MetricsConfigDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author wangpengkai
 * @date 2021/08/10
 */
@Service
public class MetricsConfigServiceImpl implements MetricsConfigService {

    private static final ILog LOGGER = LogFactory.getLog(MetricsConfigServiceImpl.class);

    private static final String DOMAIN_ACCOUNT = "domain_account";

    @Autowired
    private MetricsConfigDAO metricsConfigDAO;

    @Override
    public List<String> getMetricsByTypeAndDomainAccount(MetricsConfigInfoDTO param) {
        MetricsTypeEnum metricsTypeEnum = metricsConfigDTO2Type(param);

        Result<Void> result = paramCheck(param.getDomainAccount(), metricsTypeEnum);
        if (result.failed()) {
            return new ArrayList<>();
        }

        MetricsConfigPO metricsConfigPO = getMetricsInfoByDomainAccount(param.getDomainAccount());
        if (null == metricsConfigPO || AriusObjUtils.isNull(metricsConfigPO.getMetricInfo())) {
            return new ArrayList<>();
        }

        List<MetricsConfigInfo> metricsConfigInfos = JSON.parseArray(metricsConfigPO.getMetricInfo(), MetricsConfigInfo.class);

        // 获取对应属性下的配置列表
        List<List<String>> metricsList = metricsConfigInfos
                .stream()
                .filter(metricsConfigInfo -> metricsTypeMatch(metricsConfigInfo, metricsTypeEnum))
                .map(MetricsConfigInfo::getMetricsTypes)
                .collect(Collectors.toList());

        if (metricsList.isEmpty()) {
            LOGGER.info("对应属性的配置内容尚未入库");
            return new ArrayList<>();
        }
        return metricsList.get(0);
    }

    @Override
    public Result<Integer> updateByMetricsByTypeAndDomainAccount(MetricsConfigInfoDTO param) {
        MetricsTypeEnum metricsTypeEnum = metricsConfigDTO2Type(param);

        Result<Void> result = paramCheck(param.getDomainAccount(), metricsTypeEnum);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        MetricsConfigPO metricsConfigPO = getMetricsInfoByDomainAccount(param.getDomainAccount());
        if (null == metricsConfigPO) {
            return insertMetricsConfigInfoWithoutCheck(param);
        }

        List<MetricsConfigInfo> metricsConfigInfos = JSON.parseArray(metricsConfigPO.getMetricInfo(), MetricsConfigInfo.class);

        boolean ifPresent = false;
        for (MetricsConfigInfo metricsConfigInfo : metricsConfigInfos) {
            if (metricsTypeMatch(metricsConfigInfo, metricsTypeEnum)) {
                metricsConfigInfo.setMetricsTypes(param.getMetricsTypes());
                ifPresent = true;
                break;
            }
        }

        if (BooleanUtils.isFalse(ifPresent)) {
            metricsConfigInfos.add(createMetricsConfigInfoWithoutCheck(param));
        }

        metricsConfigPO.setMetricInfo(JSON.toJSONString(metricsConfigInfos));
        boolean succ = (1 == metricsConfigDAO.update(metricsConfigPO, new QueryWrapper<MetricsConfigPO>().eq(DOMAIN_ACCOUNT, param.getDomainAccount())));
        return Result.build(succ, metricsConfigPO.getId());
    }

    @Override
    public void deleteByDomainAccount(String domainAccount) {
        metricsConfigDAO.delete(new QueryWrapper<MetricsConfigPO>().eq(DOMAIN_ACCOUNT, domainAccount));
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

    private Result<Void> paramCheck(String domainAccount, MetricsTypeEnum metricsTypeEnum) {
        if (AriusObjUtils.isNull(domainAccount)) {
            return Result.buildFail("用户账号为空");
        }

        if (metricsTypeEnum.getCode() == MetricsTypeEnum.UNKNOWN.getCode()) {
            return Result.buildFail("指标看板未知");
        }

        return Result.buildSucc();
    }

    private MetricsConfigPO getMetricsInfoByDomainAccount(String domainAccount) {
        return metricsConfigDAO.selectOne(new QueryWrapper<MetricsConfigPO>().eq(DOMAIN_ACCOUNT, domainAccount));
    }

    private boolean metricsTypeMatch(MetricsConfigInfo metricsConfigInfo, MetricsTypeEnum metricsTypeEnum) {
        return metricsConfigInfo.getFirstMetricsType().equals(metricsTypeEnum.getFirstMetricsType())
                && metricsConfigInfo.getSecondMetricsType().equals(metricsTypeEnum.getSecondMetricsType());
    }

    private Result<Integer> insertMetricsConfigInfoWithoutCheck(MetricsConfigInfoDTO param) {
        MetricsConfigPO metricsConfigPO = new MetricsConfigPO();
        metricsConfigPO.setDomainAccount(param.getDomainAccount());
        metricsConfigPO.setMetricInfo(JSON.toJSONString(Arrays.asList(createMetricsConfigInfoWithoutCheck(param))));
        boolean succ = (1 == metricsConfigDAO.insert(metricsConfigPO));
        return Result.build(succ, metricsConfigPO.getId());
    }

    private MetricsConfigInfo createMetricsConfigInfoWithoutCheck(MetricsConfigInfoDTO param) {
        MetricsConfigInfo metricsConfigInfo = new MetricsConfigInfo();
        metricsConfigInfo.setMetricsTypes(param.getMetricsTypes());
        metricsConfigInfo.setFirstMetricsType(param.getFirstMetricsType());
        metricsConfigInfo.setSecondMetricsType(param.getSecondMetricsType());
        metricsConfigInfo.setDomainAccount(param.getDomainAccount());
        return metricsConfigInfo;
    }
}
