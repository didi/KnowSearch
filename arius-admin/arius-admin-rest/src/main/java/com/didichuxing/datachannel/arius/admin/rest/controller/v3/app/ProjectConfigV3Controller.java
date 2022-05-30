package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * project config 配置
 *
 * @author shizeying
 * @date 2022/05/30
 */
@RestController
@RequestMapping({ V3 + "/project-config" })
@Api(tags = "应用关联config (REST)")
public class ProjectConfigV3Controller {

}