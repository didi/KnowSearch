package com.didichuxing.datachannel.arius.admin.biz.thardpart;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ThirdpartAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.ThirdpartConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdPartTemplateLogicWithMasterTemplateResourceVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplateLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplateVO;

public interface CommonManager {
    /**
     * 获取app列表,包含APP全部元信息
     * @return
     */
    Result<List<ThirdpartAppVO>> listApp();

    /**
     * 验证APP校验码接口
     * @param request
     * @param appId
     * @param appSecret
     * @return
     * @throws UnsupportedEncodingException
     */
    Result<Void> verifyApp(HttpServletRequest request, Integer appId, String appSecret) throws UnsupportedEncodingException;

    /**
     * 获取物理集群列表接口
     * @return
     */
    Result<List<ThirdPartClusterVO>> listDataCluster();

    /**
     * 获取集群接口
     * @param cluster
     * @return
     */
    Result<ThirdPartClusterVO> getDataCluster(String cluster);

    /**
     * 获取配置列表接口
     * @param param
     * @return
     */
    Result<List<ThirdpartConfigVO>> queryConfig(AriusConfigInfoDTO param);

    /**
     * 获取所有逻辑模板接口
     * @return
     */
    Result<List<ThirdpartTemplateLogicVO>> listLogicTemplate();

    /**
     * 获取所有逻辑模板接口
     * @return
     */
    Result<List<ThirdPartTemplateLogicWithMasterTemplateResourceVO>> listLogicWithMasterTemplateAndResource();

    /**
     * 获取所有物理模板接口
     * @return
     */
    Result<List<ThirdpartTemplatePhysicalVO>> listPhysicalTemplate();

    /**
     * 获取所有物理模板接口
     * @return
     */
    Result<List<ThirdpartTemplateVO>> listPhysicalWithLogic();

    /**
     * 获取主模板接口
     * @param logicId
     * @return
     */
    Result<ThirdpartTemplateVO> getMasterByLogicId(Integer logicId);

    /**
     * 获取物理模板接口
     * @param physicalId
     * @return
     */
    Result<ThirdpartTemplatePhysicalVO> getPhysicalTemplateById(Long physicalId);

    /**
     * 获取授权的模板列表接口
     * @param appId
     * @param auth
     * @param dataCenter
     * @return
     */
    Result<List<ThirdpartTemplateLogicVO>> listLogicByAppIdAuthDataCenter(Integer appId, String auth, String dataCenter);
}
