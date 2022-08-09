package com.didiglobal.logi.op.manager.infrastructure.deployment;

import com.alibaba.fastjson.JSON;
import com.didiglobal.logi.op.manager.infrastructure.exception.ZeusOperationException;
import com.didiglobal.logi.op.manager.infrastructure.util.BaseHttpUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

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

    @Value("${zeus.user:root}")
    private String zeusUser;

    @Value("${zeus.batch:1}")
    private Integer zeusBatch;

    @Value("${zeus.timeOut}")
    private Integer zeusTimeOut;

    @Value("${zeus.tolerance:0}")
    private Integer zeusTolerance;

    private static final String API_TASK = "/api/task/";

    private static final String API_TEMPLATE = "http://%s/api/grp/%s/tpl/new?token=%s";

    private static final String API_EDIT_TEMPLATE = "http://%s/api/tpl/%s/edit?token=%s";

    private static final String API_EXECUTE_TASK = "http://%s/api/task?token=%s";

    private static final String API_TASK_STATUS = "http://%s/api/task/%s/request";

    private static final String API_TEMPLATE_REMOVE = "http://%s/tpl/%s";

    @Override
    public String createTemplate(ZeusTemplate zeusTemplate) throws ZeusOperationException {
        zeusTemplate.setBatch(zeusBatch);
        zeusTemplate.setAccount(zeusUser);
        zeusTemplate.setTolerance(zeusTolerance);

        String url = String.format(API_TEMPLATE, zeusServer, zeusGrpId, zeusToken);

        ZeusResult result = getZeusResultForPost(zeusTemplate, url);

        return result.getData().toString();
    }

    @Override
    public String editTemplate(ZeusTemplate zeusTemplate) throws ZeusOperationException {
        String url = String.format(API_EDIT_TEMPLATE, zeusServer, zeusTemplate.getId(), zeusToken);

        ZeusResult result = getZeusResultForPost(zeusTemplate, url);

        return result.getData().toString();
    }

    @Override
    public Integer executeTask(ZeusTask zeusTask) throws ZeusOperationException {
        String url = String.format(API_EXECUTE_TASK, zeusServer, zeusToken);
        ZeusResult result = getZeusResultForPost(zeusTask, url);
        return Integer.parseInt(result.getData().toString());
    }

    @Override
    public ZeusTaskStatus getTaskStatus(int taskId) throws ZeusOperationException {

        String response = null;
        try {
            response = BaseHttpUtil.get(String.format(API_TASK_STATUS, zeusServer, taskId), null);
        } catch (Exception e) {
            throw new ZeusOperationException(e);
        }

        ZeusResult result = JSON.parseObject(response, ZeusResult.class);
        if (result.failed()) {
            throw new ZeusOperationException(result.getMsg());
        }

        ZeusTaskStatus zeusTaskStatus = JSON.parseObject(JSON.toJSONString(result.getData()),
                ZeusTaskStatus.class);

        return zeusTaskStatus;
    }

    @Override
    public void deleteTemplate(int templateId) throws ZeusOperationException {
        String response = null;
        try {
            response = BaseHttpUtil.deleteForString(String.format(API_TEMPLATE_REMOVE, zeusServer, templateId), null, buildHeader());
        } catch (Exception e) {
            throw new ZeusOperationException(e);
        }

        ZeusResult result = JSON.parseObject(response, ZeusResult.class);
        if (result.failed()) {
            throw new ZeusOperationException(result.getMsg());
        }
    }

    @NotNull
    private ZeusResult getZeusResultForPost(Object param, String url) throws ZeusOperationException {
        String response = null;
        try {
            response = BaseHttpUtil.postForString(url, JSON.toJSONString(param), buildHeader());
        } catch (Exception e) {
            throw new ZeusOperationException(e);
        }

        ZeusResult result = JSON.parseObject(response, ZeusResult.class);
        if (result.failed()) {
            throw new ZeusOperationException(result.getMsg());
        }
        return result;
    }


}
