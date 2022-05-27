package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.cold.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.cold.ColdManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.BaseTemplateSrvOpenDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chengxiang, zqr
 * @date 2022/5/13
 */
@Service
public class ColdManagerImpl extends BaseTemplateSrvImpl implements ColdManager {


    @Override
    public NewTemplateSrvEnum templateSrv() {
        return NewTemplateSrvEnum.TEMPLATE_COLD;
    }

    @Override
    public Result<Void> isTemplateSrvAvailable(Integer logicTemplateId) {
        return Result.buildSucc();
    }

    @Override
    protected Result<Void> openSrvImpl(List<Integer> templateIdList, BaseTemplateSrvOpenDTO openParam) {
        return Result.buildFail();
    }

    @Override
    protected Result<Void> closeSrvImpl(List<Integer> templateIdList) {
        return Result.buildFail();
    }

    @Override
    public Result<Void> move2ColdNode(Integer logicTemplateId) {
        if (!isTemplateSrvOpen(logicTemplateId)) {
            return Result.buildFail("没有开启冷热分离模板服务");
        }
        return Result.buildSucc();
    }

    @Override
    public int fetchClusterDefaultHotDay(String phyCluster) {
        return 0;
    }

    @Override
    public boolean updateHotIndexRack(Long physicalId, String tgtRack) {
        return true;
    }

}
