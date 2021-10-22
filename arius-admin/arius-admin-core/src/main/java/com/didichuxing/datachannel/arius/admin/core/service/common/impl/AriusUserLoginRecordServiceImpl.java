package com.didichuxing.datachannel.arius.admin.core.service.common.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.login.UserLoginRecord;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.apache.http.client.utils.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.account.UserLoginRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.arius.UserLoginRecordPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserLoginRecordService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.arius.AriusUserLoginRecordDAO;

/**
 * @author d06679
 * @date 2019/3/18
 */
@Service
public class AriusUserLoginRecordServiceImpl implements AriusUserLoginRecordService {

    private static final ILog       LOGGER = LogFactory.getLog(AriusUserLoginRecordServiceImpl.class);

    @Autowired
    private AriusUserLoginRecordDAO userLoginRecordDao;

    @Override
    public Long save(UserLoginRecord userLoginRecord) {

        UserLoginRecordPO userLoginRecordPo = ConvertUtil.obj2Obj(userLoginRecord, UserLoginRecordPO.class);

        try {
            userLoginRecordDao.insert(userLoginRecordPo);
        } catch (Exception e) {
            LOGGER.error("class=AriusUserLoginRecordServiceImpl||method=save||msg={}", e);
        }
        return userLoginRecordPo.getId();
    }

    @Override
    public Boolean isFirstLogin(String userName) {
        return userLoginRecordDao.getCountByNameAndTimeAndTimeEnd(userName, null, null) == 1;
    }

    @Override
    public Boolean isTodayFirstLogin(String userName) {
        DateTime now = DateTime.now();
        return userLoginRecordDao.getCountByNameAndTimeAndTimeEnd(userName,
            DateUtils.formatDate(now.toDate(), "yyyy-MM-dd"),
            DateUtils.formatDate(DateTime.now().plusDays(1).toDate(), "yyyy-MM-dd")) == 1;

    }
}
