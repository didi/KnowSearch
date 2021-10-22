package com.didichuxing.datachannel.arius.admin.core.service.feedback;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.feedback.UserFeedbackDTO;

/**
 *
 *
 * @author d06679
 * @date 2020/12/29
 */
public interface AriusUserFeedbackService {

    /**
     * 保存一个用户反馈
     * @param userFeedbackDTO 用户反馈信息
     * @return feedbackId
     */
    Result save(UserFeedbackDTO userFeedbackDTO);

    /**
     * 是否需要收集反馈
     * @param userName 用户名
     * @return 是否需要收集反馈
     */
    Result isCollectFeedback(String userName);

}
