package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.APP_CONFIG;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.ES_USER;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.DELETE;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;

import com.didichuxing.datachannel.arius.admin.biz.app.ESUserManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ConsoleESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUserConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserWithVerifyCodeVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.event.app.ESUserAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.app.ESUserDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.app.ESUserEditEvent;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * es user
 * <blockquote>
 *     <pre>
 *         由于es user 的创建统一归 super admin 进行管理分配，那么系统所有创建es user的逻辑只需要校验用户是否是超级用户即可
 *     </pre>
 * </blockquote>
 * @see 0.2 将appservice 中的能力迁移到es user manager
 * @author shizeying
 * @date 2022/05/25
 */
@Component
public class ESUserManagerImpl implements ESUserManager {
    private static final ILog LOGGER = LogFactory.getLog(ESUserManagerImpl.class);
    
    @Autowired
    private ProjectService       projectService;
    @Autowired
    private ESUserService        esUserService;
    @Autowired
    private OperateRecordService operateRecordService;
    
    /**
     * 获取所有项目下全部的es user
     *
     * @return 返回app列表
     */
    @Override
    public Result<List<ESUser>> listESUsers() {
        //获取全部项目id
        final List<ProjectBriefVO> briefVOList = projectService.getProjectBriefList();
        final List<Integer> projectIds = briefVOList.stream().map(ProjectBriefVO::getId)
                .distinct().collect(Collectors.toList());
        //根据项目下所有的es user
        List<ESUser> users = esUserService.listESUsers(projectIds);
        for (ESUser user : users) {
            final Integer projectId = user.getProjectId();
           briefVOList.stream().filter(projectBriefVO -> projectBriefVO.getId().equals(projectId))
                   .findFirst().map(ProjectBriefVO::getProjectName).ifPresent(user::setName);
            
        }
        
        return Result.buildSucc(users);
    }
    
    /**
     * @param projectId
     * @param operator
     * @return
     */
    @Override
    public Result<List<ESUser>> listESUsersByProjectId(Integer projectId, String operator) {
       
    
        ProjectVO projectVO = projectService.getProjectDetailByProjectId(projectId);
       
        
        //确定当前操作者属于该项目成员
        if (Objects.nonNull(projectId) && projectVO.getUserList().stream().map(UserBriefVO::getUserName)
                .noneMatch(userName -> StringUtils.equals(userName, operator))||!StringUtils.equals(operator,
                AuthConstant.SUPER_USER_NAME)) {
            return Result.buildFail(String.format("项目:[%s]不存在成员:[%s]", projectId, operator));
        }
        final List<ESUser> users = esUserService.listESUsers(Collections.singletonList(projectId));
        for (ESUser user : users) {
            user.setName(projectVO.getProjectName());
        
        }
        return Result.buildSucc(users);
    }
    
    /**
     * 验证APP参数是否合法
     *
     * @param esUserDTO    dto
     * @param operation 是否校验null参数;  新建的时候需要校验,编辑的时候不需要校验
     * @return 参数合法返回
     */
    @Override
    public Result<Void> validateESUser(ESUserDTO esUserDTO, OperationEnum operation) {
        return esUserService.validateESUser(esUserDTO, operation);
    }
    
   
    
    
    /**
     * 新建APP
     *
     * @param appDTO    dto
     * @param projectId
     * @param operator  操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    @Override
    public Result<Integer> registerESUser(ESUserDTO appDTO, Integer projectId, String operator) {
        
    
        final Tuple</*创建的es user*/Result<Integer>,/*创建的es user po*/ ESUserPO> resultESUserPOTuple = esUserService.registerESUser(appDTO, operator);
    
         if (resultESUserPOTuple.getV1().success()) {
            // 操作记录
            operateRecordService.save(ES_USER, ADD, resultESUserPOTuple.getV2().getId(), "", operator);
            SpringTool.publish(
                    new ESUserAddEvent(this, ConvertUtil.obj2Obj(resultESUserPOTuple.getV2(), ESUser.class)));
        }

        return resultESUserPOTuple.getV1();
    }
 
    
    /**
     * 更新 es user config
     *
     * @param configDTO configdto
     * @param operator  操作人或角色
     * @return {@code Result<Void>}
     */
    @Override
    public Result<Void> updateESUserConfig(ESUserConfigDTO configDTO, String operator) {
        //只有success时候会存在tuple._2不为null
        final Tuple<Result<Void>, ESUserPO> tuple = esUserService.updateESUserConfig(configDTO, operator);
        if (tuple.getV1().success()) {
            operateRecordService.save(APP_CONFIG, EDIT, configDTO.getId(),
                    AriusObjUtils.findChangedWithClear(tuple.getV2(), configDTO), operator);
        }
        return tuple.getV1();
    }
    
   
    
    /**
     * @param esUserDTO   es user dto
     * @param operator 操作人或角色
     * @return {@code Result<Void>}
     */
    @Override
    public Result<Void> editESUser(ESUserDTO esUserDTO, String operator) {
        if (projectService.checkProjectExist(esUserDTO.getProjectId())){
             return Result.buildFail("应用不存在");
        }
        Result<Void> checkResult = this.validateESUser(esUserDTO, EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=ESUserManagerImpl||method=editESUser||fail msg={}", checkResult.getMessage());
            return Result.buildFrom(checkResult);
        }
        //获取更新之前的po
        final ESUser oldESUser = esUserService.getEsUserById(esUserDTO.getId());
        //校验当前esUserDTO中的projectId是否存在于esUser
        //更新之后的结果获取
        final Tuple<Result<Void>/*更新的状态*/, ESUserPO/*更新之后的的ESUserPO*/> resultESUserPOTuple = esUserService.editUser(esUserDTO);
    
        if (resultESUserPOTuple.getV1().success()) {
            operateRecordService.save(ES_USER, EDIT, esUserDTO.getId(),
                    AriusObjUtils.findChangedWithClear(oldESUser, resultESUserPOTuple.getV2()), operator);
            SpringTool.publish(new ESUserEditEvent(this, ConvertUtil.obj2Obj(oldESUser, ESUser.class),
                    ConvertUtil.obj2Obj(esUserService.getEsUserById(resultESUserPOTuple.getV2().getId()),
                            ESUser.class)));
        }
        return resultESUserPOTuple.getV1();
    }
    
    /**
     * 删除项目下指定的es user
     *
     * @param esUser    esUser
     * @param projectId
     * @param operator  操作人
     * @return 成功 true  失败 false
     */
    @Override
    public Result<Void> deleteESUserByProject(int esUser, int projectId, String operator) {
        
        //校验当前项目下所有的es user
        final List<ESUser> esUsers = esUserService.listESUsers(Collections.singletonList(projectId));
        if (esUsers.size()==1){
             return Result.buildFail(String.format("当前项目[%s]下只存在一个es user,不能被删除", projectId));
        }
        //校验当前项目中存在该es user
        if (esUsers.stream().map(ESUser::getId).noneMatch(esUserName -> Objects.equals(esUserName, esUser))) {
            return Result.buildParamIllegal(String.format("当前项目[%s]不存在es user:[%s]", projectId, esUsers));
        }
        //判断删除之后的es user是否为项目使用的es user,如果是项目使用的默认es user，则需要解绑项目默认的es user 后才能进行es user的删除
        if (esUsers.stream().anyMatch(oldESUser -> Objects.equals(oldESUser.getId(), esUser) && Boolean.TRUE.equals(
                oldESUser.getDefaultDisplay()))) {
        
            return Result.buildFail(String.format("项目[%s]中es user:[%s],属于项目默认的es user,请先进行解绑", projectId, esUser));
        }
        //进行es user的删除
        final Tuple<Result<Void>, ESUserPO> resultESUserPOTuple = esUserService.deleteESUserById(esUser);
        if (resultESUserPOTuple.getV1().success()){
            operateRecordService.save(ES_USER, DELETE, projectId, String.format("删除项目[%s]下es user:[%s]", projectId,esUser),
                    operator);
            SpringTool.publish(new ESUserDeleteEvent(this, ConvertUtil.obj2Obj(resultESUserPOTuple.getV2(), ESUser.class)));
        }
        return resultESUserPOTuple.getV1();
    }
    
    /**
     * 删除项目下所有的es user
     *
     * @param projectId 项目id
     * @param operator  操作人或角色
     * @return {@code Result<Void>}
     */
    @Override
    public Result<Void> deleteAllESUserByProject(int projectId, String operator) {
        
        final Tuple<Result<Void>, List<ESUserPO>> resultListTuple = esUserService.deleteByESUsers(projectId);
        if (resultListTuple.getV1().success()) {
            operateRecordService.save(ES_USER, DELETE, projectId, String.format("删除项目[%s]下的所有es user", projectId),
                    operator);
            for (ESUserPO esUserPO : resultListTuple.getV2()) {
                SpringTool.publish(new ESUserDeleteEvent(this, ConvertUtil.obj2Obj(esUserPO, ESUser.class)));
            
            }
        }
        return resultListTuple.getV1();
    }
    
    /**
     * 获取esUserName配置信息
     *
     * @param esUserName esUserName
     * @return 配置信息
     */
    @Override
    public ESUserConfig getESUserConfig(int esUserName) {
        
        return esUserService.getESUserConfig(esUserName);
    }


    
    /**
     * 校验验证码
     *
     * @param esUserName app
     * @param verifyCode 验证码
     * @return result
     */
    @Override
    public Result<Void> verifyAppCode(Integer esUserName, String verifyCode) {
        return esUserService.verifyAppCode(esUserName,verifyCode);
    }
    
   
    
    @Override
    public Result<ConsoleESUserVO> get(Integer esUser) {
        return Result.buildSucc(ConvertUtil.obj2Obj(esUserService.getEsUserById(esUser), ConsoleESUserVO.class));
    }
    
   
    /**
     * @param projectId
     * @param operator
     * @return
     */
    @Override
    public Result<List<ConsoleESUserWithVerifyCodeVO>> getNoCodeESUser(Integer projectId, String operator) {
       
        final ProjectVO projectVO = projectService.getProjectDetailByProjectId(projectId);
        if (projectService.getProjectDetailByProjectId(projectId).getUserList().stream().noneMatch(user->StringUtils.equals(user.getUserName(),operator))||StringUtils.equals(AuthConstant.SUPER_USER_NAME,operator)){
            return Result.buildParamIllegal("权限不足");
        }
        List<ESUser> users = esUserService.listESUsers(Collections.singletonList(projectId));
        for (ESUser user : users) {
            user.setName(projectVO.getProjectName());
        
        }
        return Result.buildSucc(
                ConvertUtil.list2List(users, ConsoleESUserWithVerifyCodeVO.class));
       
    }
}