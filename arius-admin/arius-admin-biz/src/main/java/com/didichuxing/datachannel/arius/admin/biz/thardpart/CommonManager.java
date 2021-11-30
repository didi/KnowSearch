package com.didichuxing.datachannel.arius.admin.biz.thardpart;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.ThirdpartAppVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ThirdPartClusterVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ThirdPartLogicClusterVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.config.ThirdpartConfigVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ThirdPartTemplateLogicWithMasterTemplateResourceVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ThirdpartTemplateLogicVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ThirdpartTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ThirdpartTemplateVO;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface CommonManager {

    /**
     * 保存操作记录接口
     * @param param
     * @return
     */
    Result<Void> addOperateRecord(OperateRecordDTO param);

    /**
     * 获取逻辑集群列表
     * @return
     */
    Result<List<ThirdPartLogicClusterVO>> listLogicCluster();

    /**
     * 获取逻辑集群列表(带上逻辑集群对应的rack信息)
     * @return
     */
    Result<List<ThirdPartLogicClusterVO>> listLogicClusterWithRack();

    /**
     * 根据物理集群名称获取rack匹配到的逻辑集群
     * @param cluster
     * @param rack
     * @return
     */
    Result<ThirdPartLogicClusterVO> queryLogicCluster(String cluster, String rack);

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
     * 获取所有逻辑模板接口
     * @param template
     * @return
     */
    Result<List<ThirdpartTemplateLogicVO>> listLogicByName(String template);

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
