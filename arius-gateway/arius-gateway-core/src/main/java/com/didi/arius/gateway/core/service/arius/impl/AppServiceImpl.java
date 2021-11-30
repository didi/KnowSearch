package com.didi.arius.gateway.core.service.arius.impl;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.exception.IndexNotPermittedException;
import com.didi.arius.gateway.common.exception.InvalidParameterException;
import com.didi.arius.gateway.common.exception.UnauthorizedException;
import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.AuthRequest;
import com.didi.arius.gateway.common.metadata.BaseContext;
import com.didi.arius.gateway.common.metadata.FlowThreshold;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.common.utils.Regex;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.RateLimitService;
import com.didi.arius.gateway.core.service.arius.AppService;
import com.didi.arius.gateway.core.service.arius.IndexTemplateService;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.AppDetailResponse;
import com.didi.arius.gateway.remote.response.AppListResponse;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@NoArgsConstructor
public class AppServiceImpl implements AppService {

    private static final Logger statLogger = LoggerFactory.getLogger(QueryConsts.STAT_LOGGER);
    private static final Logger bootLogger = LoggerFactory.getLogger(QueryConsts.BOOT_LOGGER);
    private static final Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);

    @Autowired
    private AriusAdminRemoteService ariusAdminRemoteService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private ThreadPool threadPool;

    @Value("${arius.gateway.adminSchedulePeriod}")
    private long schedulePeriod;

    private Map<Integer, AppDetail> appDetails 		= new HashMap<>();
    private Map<String, AppDetail> 	ipToAppMap  	= new HashMap<>();

    @PostConstruct
    public void init(){
        threadPool.submitScheduleAtFixTask(this::resetAppInfo, 0, schedulePeriod);
    }

    @Override
    public AppDetail getAppDetail(int appid) {
        return appDetails.get(appid);
    }

    @Override
    public Map<Integer, AppDetail> getAppDetails() {
        return appDetails;
    }

    @Override
    public AppDetail getAppDetailFromIp(String ip) {
        for(Map.Entry<String, AppDetail> entry : ipToAppMap.entrySet()){
            String mask = entry.getKey();
            if (Regex.ipMaskMatch(ip, mask)) {
                return ipToAppMap.get(mask);
            }
        }

        return null;
    }

    @Override
    public void resetAppInfo(){
        try {
            resetAppDetails();
        } catch (Exception e) {
            bootLogger.error("resetAppDetails error", e);
        }

    }

    @Override
    public void checkToken(BaseContext baseContext) {
        String authentication = baseContext.getAuthentication();
        logger.debug("token is null, check authentication, authentication={}", authentication);

        if (authentication != null) {
            AuthRequest authRequest = Convert.parseAuth(authentication);
            if (authRequest == null) {
                throw new UnauthorizedException("authentication format error!, authentication=" + authentication);
            }

            int appid = authRequest.getAppid();
            AppDetail appDetail = getAppDetail(appid);
            if (appDetail == null) {
                throw new UnauthorizedException("appid not find, appid=" + appid);
            }

            if (!appDetail.getVerifyCode().equals(authRequest.getAppsecret())) {
                throw new UnauthorizedException("auth pass error, appid=" + authRequest.getAppid() + ", appsecret=" + authRequest.getAppsecret());
            }

            baseContext.setAppDetail(appDetail);

            return ;
        }

        logger.debug("token is null, check ip, ip={}", baseContext.getRemoteAddr());

        // check ip
        AppDetail appDetail = getAppDetailFromIp(baseContext.getRemoteAddr());

        if (appDetail == null) {
            throw new UnauthorizedException("authentication exception, you need pass auth info(appid and appsecret)!");
        }

        baseContext.setAppDetail(appDetail);
    }

    @Override

    public void checkWriteIndices(BaseContext baseContext, List<String> indices) {
        List<String> indexExps = baseContext.getAppDetail().getWindexExp();
        checkIndicesPrivilege(baseContext, indices, indexExps);
    }

    @Override
    public void checkIndices(BaseContext baseContext, List<String> indices) {
        List<String> indexExps = baseContext.getAppDetail().getIndexExp();
        checkIndicesPrivilege(baseContext, indices, indexExps);
    }

    /************************************************************** private method **************************************************************/
    /**
     * 更新appid信息
     */
    private void resetAppDetails() {
        AppListResponse appListResponse = ariusAdminRemoteService.listApp();

        Map<Integer, AppDetail> newAppDetails = new HashMap<>();
        for (AppDetailResponse appDetailResponse : appListResponse.getData()) {
            AppDetail appDetail = new AppDetail();
            appDetail.setId(appDetailResponse.getId());
            appDetail.setIndexExp(appDetailResponse.getIndexExp() == null ? new ArrayList<>() : appDetailResponse.getIndexExp());
            appDetail.setWindexExp(appDetailResponse.getWindexExp() == null ? new ArrayList<>() : appDetailResponse.getWindexExp());
            appDetail.setVerifyCode(appDetailResponse.getVerifyCode());
            appDetail.setIp(appDetailResponse.getIp() == null ? new ArrayList<>(): appDetailResponse.getIp());
            appDetail.setCluster(appDetailResponse.getCluster());
            appDetail.setQueryThreshold(appDetailResponse.getQueryThreshold());
            appDetail.setDslAnalyzeEnable(appDetailResponse.getDslAnalyzeEnable() == 1);

            appDetail.setAggrAnalyzeEnable(appDetailResponse.getAggrAnalyzeEnable() == 1);
            appDetail.setAnalyzeResponseEnable(appDetailResponse.getAnalyzeResponseEnable() == 1);
            appDetail.setSearchType(AppDetail.RequestType.integerToType(appDetailResponse.getSearchType()));
            appDetail.setIsRoot(appDetailResponse.getIsRoot());

            FlowThreshold flowThreshold = new FlowThreshold();
            if (appDetail.getQueryThreshold() > 0) {
                flowThreshold.setOpsUpper(appDetail.getQueryThreshold());
                flowThreshold.setOpsLower(appDetail.getQueryThreshold());
            }

            newAppDetails.put(appDetail.getId(), appDetail);

            // set appid flow limit
            rateLimitService.resetAppAreaFlow(appDetail.getId(), flowThreshold);
        }

        String appDetailLog = JSON.toJSONString(newAppDetails);
        bootLogger.info("resetAppDetails done,old appDetails size={}, new appDetails size={}, detail={}", appDetails.size(), newAppDetails.size(), appDetailLog);

        appDetails = newAppDetails;

        resetIpToAppMap();
    }

    /**
     * 更新ip到appid的映射关系
     */
    private void resetIpToAppMap() {
        Map<String, AppDetail> newIpToAppMap = new HashMap<>();
        for (Map.Entry<Integer, AppDetail> entry : appDetails.entrySet()) {
            AppDetail appDetail = entry.getValue();
            for (String ip : appDetail.getIp()) {
                newIpToAppMap.put(ip, appDetail);
            }
        }

        ipToAppMap = newIpToAppMap;
    }

    private void checkIndicesPrivilege(BaseContext baseContext, List<String> indices, List<String> indexExps) {
        if (indices == null || indices.isEmpty()) {
            throw new InvalidParameterException("no index to query");
        }

        StringBuilder buffer = new StringBuilder();
        for (String index : indices) {
            rejectAllIndexQuery(baseContext, index);

            buffer.append(index);
            buffer.append(",");
            boolean matched = indexTemplateService.checkIndex(index, indexExps);
            if (!matched) {
                throw new IndexNotPermittedException("appid=" + baseContext.getAppDetail().getId() + " don't have permission to access " + index);
            }
        }

        String strIndices = buffer.toString();
        if (strIndices.endsWith(",")) {
            strIndices = strIndices.substring(0, strIndices.length() -1);
        }

        if (baseContext.isDetailLog()) {
            statLogger.info(QueryConsts.DLFLAG_PREFIX + "query_request_indices||appid={}||requestId={}||indices={}||types={}",
                    baseContext.getAppid(), baseContext.getRequestId(), strIndices, baseContext.getTypeNames());
        }
    }

    private void rejectAllIndexQuery(BaseContext baseContext, String index) {
        String tIndex= index.trim();
        for (int i = 0; i < tIndex.length(); ++i) {
            char c = tIndex.charAt(i);
            if (c != '*') {
                return ;
            }
        }

        throw new IndexNotPermittedException("appid=" + baseContext.getAppDetail().getId() + " don't have permission to access " + index);
    }
}
