/*
 * Copyright (c) 2015, WINIT and/or its affiliates. All rights reserved. Use, Copy is subject to authorized license.
 */
package com.didichuxing.datachannel.arius.admin.persistence.mysql.arius;

import com.didichuxing.datachannel.arius.admin.common.bean.po.arius.UserLoginRecordPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author fengqiongfeng
 * @date 2020-12-29
 */
@Repository
public interface AriusUserLoginRecordDAO {

    Long insert(UserLoginRecordPO param);

    int getCountByNameAndTimeAndTimeEnd(@Param("loginName")String loginName,
                                        @Param("loginTimeBegin")String loginTimeBegin,
                                        @Param("loginTimeEnd")String loginTimeEnd);
}
