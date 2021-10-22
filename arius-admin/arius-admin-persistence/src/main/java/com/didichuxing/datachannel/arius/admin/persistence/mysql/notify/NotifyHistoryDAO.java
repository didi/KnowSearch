package com.didichuxing.datachannel.arius.admin.persistence.mysql.notify;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.notify.NotifyHistoryPO;

/**
 *
 *
 * @author d06679
 * @date 2019/3/13
 */
@Repository
public interface NotifyHistoryDAO {

    int insert(NotifyHistoryPO param);

    List<NotifyHistoryPO> getByKeyAndTime(@Param("taskType") String taskType, @Param("bizId") String bizId,
                                          @Param("receiver") String receiver, @Param("channel") String channel,
                                          @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
