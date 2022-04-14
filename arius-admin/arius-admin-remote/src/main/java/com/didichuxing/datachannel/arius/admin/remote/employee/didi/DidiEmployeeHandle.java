package com.didichuxing.datachannel.arius.admin.remote.employee.didi;

import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.MAIN_DATA_ACCOUNT_URK;
import static com.didichuxing.datachannel.arius.admin.remote.InterfaceConstant.MAIN_DATA_STAFFINO_LADP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.employee.BaseEmInfo;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.util.BaseHttpUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.remote.employee.EmployeeHandle;
import com.didichuxing.datachannel.arius.admin.remote.employee.bean.EpHrEmplInfoData;
import com.didichuxing.datachannel.arius.admin.remote.employee.bean.EpHrInfoResult;
import com.didichuxing.datachannel.arius.admin.remote.employee.bean.EpHrMedaInfo;
import com.didichuxing.datachannel.arius.admin.remote.employee.bean.MainDataStaffInfo;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author linyunan
 * @date 2021-05-14
 */
@Component
public class DidiEmployeeHandle implements EmployeeHandle {
    private static final ILog LOGGER = LogFactory.getLog(DidiEmployeeHandle.class);

    private String     epMdataHttp;

    private String     epMdataAppid;

    private String     epMdataKey;

    @Override
    public <T extends BaseEmInfo> Result<T> getByDomainAccount(String domainAccount) {
        if (StringUtils.isBlank(domainAccount)) {
            return null;
        }

        EpHrEmplInfoData infoFromEp = getInfoFromEp(new TypeReference<EpHrInfoResult<EpHrEmplInfoData>>() {
        }, MAIN_DATA_STAFFINO_LADP + domainAccount);

        return (Result<T>) Result.buildSucc(infoFromEp);
    }

    @Override
    public Result<Void> checkUsers(String domainAccounts) {
        for (String user : domainAccounts.split(",")) {
            if (validate(user).failed()) {
                return Result.buildFail();
            }
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Object> searchOnJobStaffByKeyWord(String keyWord) {
        List<MainDataStaffInfo> mainDataStaffInfos = getInfoFromEp(
            new TypeReference<EpHrInfoResult<List<MainDataStaffInfo>>>() {
            }, MAIN_DATA_ACCOUNT_URK + keyWord);

        if (CollectionUtils.isEmpty(mainDataStaffInfos)) {
            return Result.buildFail();
        }

        return Result.buildSucc(mainDataStaffInfos);
    }

    /**************************************** private methods ****************************************/
    private <T> T getInfoFromEp(TypeReference<EpHrInfoResult<T>> type, String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        try {
            String time = String.valueOf(System.currentTimeMillis());
            String sign = DigestUtils.sha1Hex(epMdataAppid + epMdataKey + time);

            Map<String, String> headers = new HashMap<>();
            headers.put("Ver", "V3");
            headers.put("AppId", epMdataAppid);
            headers.put("Timestamp", time);
            headers.put("Signature", sign);

            String resp = BaseHttpUtil.get(epMdataHttp + url, null, headers);

            LOGGER.info("class=DidiEmployeeHandle||method=getInfoFromEp||url={}||resp={}", url, resp);

            EpHrInfoResult<T> epRet = JSON.parseObject(resp, type);
            if (null == epRet || null == epRet.getMeta() || null == epRet.getData()) {
                return null;
            }

            EpHrMedaInfo medaInfo = epRet.getMeta();
            if (0 != medaInfo.getCode()) {
                return null;
            }

            return epRet.getData();
        } catch (Exception e) {
            LOGGER.error("class=DidiEmployeeHandle||method=getInfoFromEp||errMsg={}||url={}", e.getMessage(), url, e);
        }

        return null;
    }

    private Result<Boolean> validate(String domainAccount) {
        if (EnvUtil.isDev() || EnvUtil.isTest() || EnvUtil.isStable() || EnvUtil.isPressure()) {
            return Result.buildSucc();
        }

        Result<EpHrEmplInfoData> emplResult = getByDomainAccount(domainAccount);
        if (null == emplResult || null == emplResult.getData()) {
            LOGGER.warn("class=DidiEmployeeHandle||method=validate||domainAccount={}||msg=user name illegal",
                domainAccount);
            return Result.buildFail();
        }
        EpHrEmplInfoData epHrEmplInfoData = emplResult.getData();
        if (!EpHrEmplInfoData.USER_ON_LINE.equals(epHrEmplInfoData.getHrStatus())) {
            LOGGER.warn("class=DidiEmployeeHandle||method=validate||domainAccount={}||msg=user name status not online",
                domainAccount);
            return Result.buildFail();
        }

        return Result.buildSucc(true);
    }
}
