package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.BaseTemplateSrvOpenDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chengxiang
 * @date 2022/5/11
 */
public abstract class BaseTemplateSrvImpl implements BaseTemplateSrv {

    protected static final ILog LOGGER = LogFactory.getLog(BaseTemplateSrvImpl.class);
    private FutureUtil<Void> BASE_TEMPLATE_SRV_IMPL_FUTURE_UTIL = FutureUtil.init("BASE_TEMPLATE_SRV_IMPL_FUTURE_UTIL",10,10,100);

    @Autowired
    protected IndexTemplateService indexTemplateService;

    @Autowired
    protected IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    protected TemplatePhyManager templatePhyManager;

    @Autowired
    protected TemplateSrvManager templateSrvManager;

    @Override
    public boolean isTemplateSrvOpen(Integer templateId) {
        return templateSrvManager.isTemplateSrvOpen(templateId, templateSrv().getCode());
    }

    @Override
    public String templateSrvName() {
        return templateSrv().getServiceName();
    }

    @Override
    //todo: 思考这里失败了如何处理，如何回滚
    public Result<Void> openSrv(List<Integer> templateIdList, BaseTemplateSrvOpenDTO openParam) {
        // 0.校验服务是否可以开启
        for (Integer templateId : templateIdList) {
            Result<Void> checkAvailableResult = isTemplateSrvAvailable(templateId);
            if (checkAvailableResult.failed()) {
                return checkAvailableResult;
            }
        }

        // 1.服务开启实际操作
        Result<Void> openResult = openSrvImpl(templateIdList, openParam);
        if (openResult.failed()) {
            return openResult;
        }

        // 2.更新DB服务开启状态
        Result<Void> updateDBResult = updateDBSrvStatus(templateIdList, Boolean.TRUE);
        if (updateDBResult.failed()) {
            return updateDBResult;
        }

        return Result.buildSucc();
    }

    @Override
    public Result<Void> closeSrv(List<Integer> templateIdList) {
        // 0.服务关闭实际操作
        Result<Void> closeResult = closeSrvImpl(templateIdList);
        if (closeResult.failed()) {
            return closeResult;
        }

        // 1.更新DB服务关闭状态
        Result<Void> updateDBResult = updateDBSrvStatus(templateIdList, Boolean.FALSE);
        if (closeResult.failed()) {
            return updateDBResult;
        }

        return Result.buildSucc();
    }

    /**
     * 服务开启实际执行体
     * @param templateIdList
     * @param openParam
     * @return
     */
    protected abstract Result<Void> openSrvImpl(List<Integer> templateIdList, BaseTemplateSrvOpenDTO openParam);

    /**
     * 服务关闭实际执行体
     * @param templateIdList
     * @return
     */
    protected abstract Result<Void> closeSrvImpl(List<Integer> templateIdList);


    ////////////////////////////private method/////////////////////////////////////

    /**
     * 更新DB服务状态
     * @param templateIdList
     * @param status, true:开启, false:关闭
     * @return
     */
    private Result<Void> updateDBSrvStatus(List<Integer> templateIdList, Boolean status) {
        AtomicBoolean isSuccess = new AtomicBoolean(true);
        String srvCode = templateSrv().getCode().toString();
        for (Integer templateId : templateIdList) {
            BASE_TEMPLATE_SRV_IMPL_FUTURE_UTIL.runnableTask(() -> {
                IndexTemplate indexTemplate = indexTemplateService.getLogicTemplateById(templateId);
                if (null == indexTemplate) {
                    isSuccess.set(false);
                    return;
                }

                Boolean modifyFlag = status ? addSrvCode(indexTemplate, srvCode) : removeSrvCode(indexTemplate, srvCode);
                if (!modifyFlag) {
                    return;
                }

                try {
                    //todo: indexTeplateDTO 加字段
                    Result<Void> updateResult = indexTemplateService.editTemplateInfoTODB(ConvertUtil.obj2Obj(indexTemplate, IndexTemplateDTO.class));
                    if (updateResult.failed()) {
                        isSuccess.set(false);
                    }
                } catch (Exception e) {
                    isSuccess.set(false);
                    LOGGER.error("updateDBSrvStatus error, templateId:{}, status:{}", templateId, status, e);
                }
            });
        }
        BASE_TEMPLATE_SRV_IMPL_FUTURE_UTIL.waitExecute();
        return isSuccess.get() ? Result.buildSucc() : Result.buildFail("更新DB服务状态失败");
    }

    /**
     * 添加开启服务到对应模板实体中
     * @param indexTemplate
     * @param addSrvCode
     * @return true:有修改, false:无修改；根据返回值判断是否需要刷新到DB
     */
    private Boolean addSrvCode(IndexTemplate indexTemplate, String addSrvCode) {
        Boolean modifiedFlag = Boolean.FALSE;
        String srvCodeStr = indexTemplate.getOpenSrv();
        List<String> srvCodeList = ListUtils.string2StrList(srvCodeStr);
        if (srvCodeList.isEmpty()) {
            indexTemplate.setOpenSrv(addSrvCode);
            modifiedFlag = Boolean.TRUE;
        } else {
            if (!srvCodeList.contains(addSrvCode)) {
                indexTemplate.setOpenSrv(srvCodeStr + "," + addSrvCode);
                modifiedFlag = Boolean.TRUE;
            }
        }

        return modifiedFlag;
    }

    private Boolean removeSrvCode(IndexTemplate indexTemplate, String removeSrvCode) {
        Boolean modifiedFlag = Boolean.FALSE;
        String srvCodeStr = indexTemplate.getOpenSrv();
        List<String> srvCodeList = ListUtils.string2StrList(srvCodeStr);
        if (srvCodeList.contains(removeSrvCode)) {
            srvCodeList.remove(removeSrvCode);
            indexTemplate.setOpenSrv(ListUtils.strList2String(srvCodeList));
            modifiedFlag = Boolean.TRUE;
        }

        return modifiedFlag;
    }

}
