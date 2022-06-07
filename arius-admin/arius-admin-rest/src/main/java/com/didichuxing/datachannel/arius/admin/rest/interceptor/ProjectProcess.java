package com.didichuxing.datachannel.arius.admin.rest.interceptor;

import static com.didiglobal.logi.security.common.constant.Constants.API_PREFIX;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.app.ProjectConfigManager;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppSearchTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didiglobal.logi.security.common.PagingData;
import com.didiglobal.logi.security.common.Result;
import com.didiglobal.logi.security.common.constant.OplogConstant;
import com.didiglobal.logi.security.common.dto.oplog.OplogQueryDTO;
import com.didiglobal.logi.security.common.vo.oplog.OplogVO;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.service.OplogService;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ProjectProcess implements ResponseBodyAdvice {
    
    @Autowired
    private              ESUserService        esUserService;
    @Autowired
    private              ProjectConfigManager projectConfigManager;
    @Autowired
    private              OplogService         oplogService;
    private static final String PROJECT_END      = "project";
    private static final String PROJECT_END_LIST      = "project/list";
    private static final String SLOW_QUERY_TIMES = "slowQueryTimes";
    private static final String MEMO             = "memo";
    private static final String id               = "id";
    private static final String IS_SUPER         = "isSuper";
    private static final String REGULAR_PROJECT_USER_PATH="/user/\\d*$";
    
    
    @Autowired
    private RoleTool roleTool;
    
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }
    
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        
        String requestPath = ((ServletServerHttpRequest) request).getServletRequest().getServletPath();
        
        if (requestPath.startsWith(API_PREFIX)) {
            //如何是创建项目的接口会一并创建projectid
            if (requestPath.endsWith(PROJECT_END) && HttpMethod.POST.equals(request.getMethod())
                && body instanceof com.didiglobal.logi.security.common.Result) {
                Object data = ((Result<?>) body).getData();
                if (Objects.nonNull(data) && ((Result<?>) body).successed() && data instanceof ProjectVO) {
                    //通过RequestContextHolder获取request
                    registerESUserDefault((ProjectVO) data);
                    //获取description字段之后进行提取其中的两个配置
                    final String description = ((ProjectVO) data).getDescription();
                    if (StringUtils.isNotBlank(description)) {
        
                        creatProjectConfig((ProjectVO) data);
        
                    }
                    
                }
    
            }
            String[] prefix = StringUtils.split(requestPath, "/");
            String projectId = prefix[prefix.length - 1];
            //删除项目的时候需要一并删除项目的配置
            if (requestPath.contains(PROJECT_END) && HttpMethod.DELETE.equals(request.getMethod())
                && body instanceof com.didiglobal.logi.security.common.Result && ((Result<?>) body).successed()
                && StringUtils.isNumeric(projectId)) {
                Optional.ofNullable(prefix[prefix.length - 1]).map(Integer::parseInt)
                        .ifPresent(projectConfigManager::deleteByProjectId);
        
            }
            //对logi侧返回的project信息进行后置处理，判断是否为super project
            if (requestPath.contains(PROJECT_END_LIST) && HttpMethod.GET.equals(request.getMethod())
                && body instanceof com.didiglobal.logi.security.common.Result && ((Result<?>) body).successed()) {
                return projectBriefVOAddIsSuperField(body);
            }
            //对logi侧返回的user/{userId}接口进行后置处理，返回是否为super project
            if (Pattern.compile(REGULAR_PROJECT_USER_PATH).matcher(requestPath).find() && HttpMethod.GET.equals(request.getMethod())
                && body instanceof com.didiglobal.logi.security.common.Result && ((Result<?>) body).successed()) {
                return projectBriefVOAddIsSuperField(body);
            }
        }
        
        return body;
    }
    
    /**
     * 新增默认的es user
     *
     * @param data 数据
     */
    private void registerESUserDefault(ProjectVO data) {
        ESUserDTO esUserDTO = new ESUserDTO();
        esUserDTO.setIsRoot(0);
        esUserDTO.setSearchType(AppSearchTypeEnum.TEMPLATE.getCode());
        esUserDTO.setVerifyCode(RandomStringUtils.randomAlphabetic(7));
        esUserDTO.setMemo(((ProjectVO) data).getProjectName() + "项目默认的es user");
        esUserDTO.setProjectId(((ProjectVO) data).getId());
        esUserService.registerESUser(esUserDTO, roleTool.adminList().get(0).getUserName());
    }
    
    /**
     * 项目创建完成后，通过Description字段进行后置写入projectconfig
     *
     * @param data 数据
     */
    private void creatProjectConfig(ProjectVO data) {
        final OplogQueryDTO oplogQueryDTO = new OplogQueryDTO();
        oplogQueryDTO.setDetail(((ProjectVO) data).getProjectName());
        oplogQueryDTO.setTargetType(OplogConstant.PM_A);
        oplogQueryDTO.setTarget(OplogConstant.PM_P);
        oplogQueryDTO.setOperateType(OplogConstant.PM);
        final PagingData<OplogVO> oplogPage = oplogService.getOplogPage(oplogQueryDTO);
        //获取操作人
        String operator;
        if (oplogPage.getBizData().isEmpty()) {
            operator = ((ProjectVO) data).getOwnerList().stream().findFirst().orElse(new UserBriefVO()).getUserName();
        } else {
            operator = oplogPage.getBizData().stream().findFirst().orElse(new OplogVO()).getOperator();
        }
        final JSONObject jsonObject = JSON.parseObject(data.getDescription());
        final Integer slowQueryTimes = jsonObject.getInteger(SLOW_QUERY_TIMES);
        final String memo = jsonObject.getString(MEMO);
        final ProjectConfigDTO projectConfigDTO = new ProjectConfigDTO();
        projectConfigDTO.setProjectId(((ProjectVO) data).getId());
        projectConfigDTO.setSlowQueryTimes(slowQueryTimes);
        projectConfigDTO.setMemo(memo);
        
        projectConfigManager.initProjectConfig(projectConfigDTO, operator);
    }
    
    private Object projectBriefVOAddIsSuperField(Object body){
        Object data = ((Result<?>) body).getData();
        List projectBriefVOs = Lists.newArrayList();
        if (Objects.nonNull(data) &&  data instanceof List) {
            if (CollectionUtils.isNotEmpty((List) data)) {
                for (Object projectBrief : ((List) data)) {
                    final String projectStr = JSON.toJSONString(projectBrief);
                    final JSONObject jsonObject = JSON.parseObject(projectStr);
                    final Integer projectId = jsonObject.getInteger(id);
                    if (Objects.nonNull(projectId) && AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
                        jsonObject.put(IS_SUPER, true);
            
                    } else {
                        jsonObject.put(IS_SUPER, false);
                    }
                    projectBriefVOs.add(jsonObject);
                }
                return  Result.buildSucc(projectBriefVOs);
            }
            
        }
        return body;
        
    }
}