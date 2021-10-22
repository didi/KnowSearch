/*
 * Copyright (c) 2015, WINIT and/or its affiliates. All rights reserved. Use, Copy is subject to authorized license.
 */
package com.didichuxing.datachannel.arius.admin.persistence.mysql.feedback;


import com.didichuxing.datachannel.arius.admin.common.bean.po.feedback.AriusUserFeedbackPO;
import org.springframework.stereotype.Repository;

/**
 * AriusUserFeedback Dao
 * 
 * @author fengqiongfeng
 * @date 2020-12-29
 */
@Repository
public interface AriusUserFeedbackDAO {

    Long insert(AriusUserFeedbackPO param);
}
