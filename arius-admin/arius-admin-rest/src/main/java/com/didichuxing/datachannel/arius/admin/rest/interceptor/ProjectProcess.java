package com.didichuxing.datachannel.arius.admin.rest.interceptor;

import static com.didiglobal.logi.security.common.constant.Constants.API_PREFIX;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppSearchTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectConfigService;
import com.didiglobal.logi.security.common.Result;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
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
    private              ESUserService        esUserService;
    @Autowired
    private              ProjectConfigService projectConfigService;
    private static final String               PROJECT_END = "project";
    @Autowired
    private RoleTool roleTool;
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }
    
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        
        String requestPath = request.getURI().getPath();
        
        if (requestPath.startsWith(API_PREFIX)) {
            //如何是创建项目的接口会一并创建projectid
            if (requestPath.endsWith(PROJECT_END) && HttpMethod.POST.equals(request.getMethod())
                && body instanceof com.didiglobal.logi.security.common.Result) {
                Object data = ((Result<?>) body).getData();
                if (Objects.nonNull(data) && ((Result<?>) body).successed() && data instanceof ProjectVO) {
                    //通过RequestContextHolder获取request
                    ESUserDTO esUserDTO = new ESUserDTO();
                    esUserDTO.setIsRoot(0);
                    esUserDTO.setSearchType(AppSearchTypeEnum.TEMPLATE.getCode());
                    esUserDTO.setVerifyCode(RandomStringUtils.randomAlphabetic(7));
                    esUserDTO.setMemo(((ProjectVO) data).getProjectName() + "项目默认的es user");
                    esUserDTO.setProjectId(((ProjectVO) data).getId());
                    esUserService.registerESUser(esUserDTO,roleTool.adminList().get(0).getUserName() );
                }
                
            }
            String[] prefix = StringUtils.split(requestPath, "/");
            String projectId = prefix[prefix.length - 1];
            //删除项目的时候需要一并删除项目的配置
            if (requestPath.contains(PROJECT_END) && HttpMethod.DELETE.equals(request.getMethod())
                && body instanceof com.didiglobal.logi.security.common.Result && ((Result<?>) body).successed()
                && StringUtils.isNumeric(projectId)) {
                Optional.ofNullable(prefix[prefix.length - 1]).map(Integer::parseInt)
                        .ifPresent(projectConfigService::deleteByProjectId);
                
            }
        }
        
        return body;
    }
}