package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.feedback.UserFeedbackDTO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.feedback.AriusUserFeedbackService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V3_NORMAL + "/feedback")
@Api(value = "用户反馈接口")
public class NormalFeedbackController {

    @Autowired
    private AriusUserFeedbackService userFeedbackService;

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "添加反馈", notes = "")
    public Result add(HttpServletRequest request,@RequestBody UserFeedbackDTO userFeedbackDTO) {
        String username = HttpRequestUtils.getOperator(request);
        userFeedbackDTO.setCreator(username);
        return userFeedbackService.save(userFeedbackDTO);
    }

    @GetMapping("/isCollectFeedback")
    @ResponseBody
    @ApiOperation(value = "是否收集反馈", notes = "是否收集反馈")
    public Result<Boolean> isCollectFeedback(HttpServletRequest request) {
        String username = HttpRequestUtils.getOperator(request);
        return userFeedbackService.isCollectFeedback(username);
    }
}
