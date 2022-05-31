package com.didichuxing.datachannel.arius.admin.rest.interceptor;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppSearchTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didiglobal.logi.security.common.Result;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import java.util.Objects;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ProjectProcess implements ResponseBodyAdvice {

    @Autowired
    private ESUserService esUserService;
    private static final String PROJECT_END = "project";

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        String requestPath = request.getURI().getPath();
    
        //如何是创建项目的接口会一并创建projectid
        if (requestPath.endsWith(PROJECT_END) && body instanceof com.didiglobal.logi.security.common.Result&&HttpMethod.POST.equals(request.getMethod())) {
            Object data = ((Result<?>) body).getData();
            if (Objects.nonNull(data) && data instanceof ProjectVO) {
                //通过RequestContextHolder获取request
                ESUserDTO esUserDTO = new ESUserDTO();
                esUserDTO.setIsRoot(0);
                esUserDTO.setSearchType(AppSearchTypeEnum.TEMPLATE.getCode());
                esUserDTO.setVerifyCode(RandomStringUtils.randomAlphabetic(7));
                esUserDTO.setResponsible(AuthConstant.SUPER_USER_NAME);
                esUserDTO.setMemo(((ProjectVO) data).getProjectName()+"项目默认的es user");
                esUserDTO.setProjectId(((ProjectVO) data).getId());
                esUserService.registerESUser(esUserDTO, AuthConstant.SUPER_USER_NAME);
            }
        
            return body;
        }
        //删除项目的时候需要一并删除项目的配置
        //if (&&HttpMethod.DELETE.equals(request.getMethod()))
        return body;
    }
}