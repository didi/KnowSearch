package com.didi.arius.gateway.core.service.arius.impl;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.MappingIndexNameWhiteAppIds;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.component.ThreadPool;
import com.didi.arius.gateway.core.service.arius.DynamicConfigService;
import com.didi.arius.gateway.remote.AriusAdminRemoteService;
import com.didi.arius.gateway.remote.response.DynamicConfigListResponse;
import com.didi.arius.gateway.remote.response.DynamicConfigResponse;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
public class DynamicConfigServiceImpl implements DynamicConfigService {

    protected static final ILog bootLogger = LogFactory.getLog( QueryConsts.BOOT_LOGGER);

    @Autowired
    private AriusAdminRemoteService ariusAdminRemoteService;

    @Autowired
    private ThreadPool threadPool;

    @Value("${arius.gateway.adminSchedulePeriod}")
    private long schedulePeriod;

    private String host = Convert.getHostName();

    private String forbiddenSettings;

    private boolean detailLogFlag = false;

    /**
     * 可以跳过多type索引启用映射查询的appid列表
     */
    private MappingIndexNameWhiteAppIds whiteAppIds = new MappingIndexNameWhiteAppIds();

    @PostConstruct
    public void init(){
        threadPool.submitScheduleAtFixTask(this::resetDynamicConfigInfo, 5, schedulePeriod);
    }

    @Override
    public boolean getDetailLogFlag() {
        return detailLogFlag;
    }

    /**
     * 是否可以跳过检查
     *
     * @param appid
     * @return
     */
    @Override
    public boolean isWhiteAppid(int appid) {
        return whiteAppIds.isWhiteAppid(appid);
    }

    @Override
    public void resetDynamicConfigInfo(){
        resetDynamicConfigInfoInner();
    }

    /************************************************************** private method **************************************************************/
    /**
     * 从admin拉取gateway动态配置信息，并更新
     */
    private void resetDynamicConfigInfoInner() {
        try {
            bootLogger.info("resetDynamicConfigInfo begin...");
            DynamicConfigListResponse response = ariusAdminRemoteService.listQueryConfig();
            if (response.getCode() != 0) {
                return ;
            }

            for (DynamicConfigResponse dynamicConfigResponse : response.getData()) {
                if (dynamicConfigResponse.getValueName() == null) {
                    bootLogger.warn("GateWayHeartBeatService dynamicConfig value null, id={}", dynamicConfigResponse.getId());
                    continue;
                }

                try {
                    if (dynamicConfigResponse.getValueName().equals(QueryConsts.DETAIL_LOG_FLAG)) {
                        bootLogger.info("resetDynamicConfigInfo reset {} begin...", QueryConsts.DETAIL_LOG_FLAG);

                        Map<String, Object> value = JSON.parseObject(dynamicConfigResponse.getValue());
                        if (value.containsKey(host)) {
                            detailLogFlag = (boolean) value.get(host);
                        } else {
                            detailLogFlag = false;
                        }

                        bootLogger.info("resetDynamicConfigInfo reset {} end, detailLogFlag={}", QueryConsts.DETAIL_LOG_FLAG, detailLogFlag);
                        // 动态配置项，可以跳过多type索引启用映射查询的appid列表
                    } else if (QueryConsts.MAPPING_INDEXNAME_WHITE_APPIDS.equals(dynamicConfigResponse.getValueName())) {
                        dealWhiteAppId(dynamicConfigResponse);
                    }else if(QueryConsts.FORBIDDEN_SETTING_PATH.equals(dynamicConfigResponse.getValueName())){
                        forbiddenSettings = dynamicConfigResponse.getValue();
                    }

                } catch (Exception e) {
                    bootLogger.error("dynamicConfig process value||name={}||value={}||e={}", dynamicConfigResponse.getValueName(), dynamicConfigResponse.getValue(), Convert.logExceptionStack(e));
                }

            }

            bootLogger.info("resetDynamicConfigInfo end...");
        } catch (Exception e) {
            bootLogger.error("resetDynamicConfigInfo_error||e={}", Convert.logExceptionStack(e));
        }

    }

    private void dealWhiteAppId(DynamicConfigResponse dynamicConfigResponse) {
        MappingIndexNameWhiteAppIds appIdds = JSON.parseObject(dynamicConfigResponse.getValue(), MappingIndexNameWhiteAppIds.class);
        if (!whiteAppIds.equals(appIdds)) {
            whiteAppIds.setAppids(appIdds.getAppids());
        }
    }

    public String getForbiddenSettings() {
        return forbiddenSettings;
    }

}
