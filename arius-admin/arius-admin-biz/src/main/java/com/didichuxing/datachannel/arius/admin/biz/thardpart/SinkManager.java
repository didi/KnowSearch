package com.didichuxing.datachannel.arius.admin.biz.thardpart;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.SinkSdkESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.SinkSdkIDCTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.SinkSdkTemplateDeployInfoVO;

import javax.servlet.http.HttpServletRequest;

public interface SinkManager {

    /**
     * 获取权限信息、验证码因袭，请求头中需要提供ticket
     * @param request
     * @param projectId
     * @return
     */
    Result<SinkSdkESUserVO> listApp(HttpServletRequest request, Integer projectId);

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