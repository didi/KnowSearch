package com.didiglobal.logi.op.manager.infrastructure.deployment;

import com.alibaba.fastjson.JSON;
import com.didiglobal.logi.op.manager.infrastructure.exception.ZeusOperationException;
import com.didiglobal.logi.op.manager.infrastructure.util.BaseHttpUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.didiglobal.logi.op.manager.infrastructure.util.BaseHttpUtil.buildHeader;

/**
 * @author didi
 * @date 2022-07-08 7:00 下午
 */
@Data
@Component
public class ZeusServiceImpl implements ZeusService {

    @Value("${zeus.server}")
    private String zeusServer;

    @Value("${zeus.token}")
    private String zeusToken;

    @Value("${zeus.grpId}")
    private String zeusGrpId;

    @Value("${zeus.user}")
    private String zeusUser;

    @Value("${zeus.batch}")
    private Integer zeusBatch;

    @Value("${zeus.timeOut}")
    private Integer zeusTimeOut;

    @Value("${zeus.tolerance}")
    private Integer zeusTolerance;

    private static final String API_TASK = "/api/task/";

    private static final String API_TEMPLATE = "http://%s/api/grp/%s/tpl/new?token=%s";

    private static final String API_EDIT_TEMPLATE = "http://%s/api/tpl/%s/edit?token=%s";

    @Override
    public String createTemplate(ZeusTemplate zeusTemplate) throws ZeusOperationException {
        String url = String.format(API_TEMPLATE, zeusServer, zeusGrpId, zeusToken);

        String response = BaseHttpUtil.postForString(url, JSON.toJSONString(zeusTemplate), buildHeader());

        ZeusResult result = JSON.parseObject(response, ZeusResult.class);
        if (result.failed()) {
            throw new ZeusOperationException(result.getMsg());
        }

        return result.getData().toString();
    }

    @Override
    public String ditTemplate(ZeusTemplate zeusTemplate) throws ZeusOperationException {
        String url = String.format(API_EDIT_TEMPLATE, zeusServer, zeusTemplate.getId(), zeusToken);

        String response = BaseHttpUtil.postForString(url, JSON.toJSONString(zeusTemplate), buildHeader());

        ZeusResult result = JSON.parseObject(response, ZeusResult.class);
        if (result.failed()) {
            throw new ZeusOperationException(result.getMsg());
        }

        return result.getData().toString();
    }
}
