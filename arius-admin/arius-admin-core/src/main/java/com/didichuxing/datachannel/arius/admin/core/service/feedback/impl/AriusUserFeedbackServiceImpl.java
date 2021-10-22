package com.didichuxing.datachannel.arius.admin.core.service.feedback.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.feedback.UserFeedbackDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.feedback.AriusUserFeedbackPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserLoginRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.feedback.AriusUserFeedbackService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.feedback.AriusUserFeedbackDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;

/**
 * @author d06679
 * @date 2020/12/29
 */
@Service
public class AriusUserFeedbackServiceImpl implements AriusUserFeedbackService {

    @Autowired
    private AriusConfigInfoService      ariusConfigInfoService;

    @Autowired
    private AriusUserFeedbackDAO        ariusUserFeedbackDao;

    @Autowired
    private AriusUserLoginRecordService ariusUserLoginRecordService;

    @Override
    public Result save(UserFeedbackDTO userFeedbackDTO) {
        AriusUserFeedbackPO userFeedbackPO = ConvertUtil.obj2Obj(userFeedbackDTO, AriusUserFeedbackPO.class);
        userFeedbackPO.setCreateTime(new Date());
        return Result.buildSucc(ariusUserFeedbackDao.insert(userFeedbackPO));
    }

    @Override
    public Result<Boolean> isCollectFeedback(String userName) {
        if (!ariusConfigInfoService.booleanSetting(ARIUS_COMMON_GROUP, "arius.is.collect.feedback", false)) {
            return Result.buildSucc(false);
        }
        return Result.buildSucc(ariusUserLoginRecordService.isTodayFirstLogin(userName));
    }
}
