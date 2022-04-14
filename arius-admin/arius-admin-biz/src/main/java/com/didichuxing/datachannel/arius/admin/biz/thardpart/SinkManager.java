package com.didichuxing.datachannel.arius.admin.biz.thardpart;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.SinkSdkAppVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.SinkSdkIDCTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.SinkSdkTemplateDeployInfoVO;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

public interface SinkManager {

    /**
     * 获取权限信息、验证码因袭，请求头中需要提供ticket
     * @param request
     * @param appId
     * @return
     */
    Result<SinkSdkAppVO> listApp(HttpServletRequest request, Integer appId);

    /**
     * 获取模板信息，带主从结构
     * @param templateName
     * @return
     */
    Result<SinkSdkTemplateDeployInfoVO> listDeployInfo(String templateName);

    /**
     *
     * @param templateName
     * @return
     */
    Result<SinkSdkIDCTemplateDeployInfoVO> getIDCDeployInfo(String templateName);
}
