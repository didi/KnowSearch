package com.didichuxing.datachannel.arius.admin.core.service.app.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.ADD;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.EDIT;
import static com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil.obj2Obj;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppSearchTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuple;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuple2;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.VerifyCodeFactory;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ESUserDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author linyunan
 * @date 2021-04-28
 */
@Service
@Transactional
public class ESUserServiceImpl implements ESUserService {

    private static final ILog LOGGER = LogFactory.getLog(ESUserServiceImpl.class);
    
    public static final Integer VERIFY_CODE_LENGTH = 15;
    
    private static final Integer APP_QUERY_THRESHOLD_DEFAULT = 100;
    
    private static final String ES_USER_NOT_EXIST = "es user 不存在";

    @Autowired
    private ESUserDAO esUserDAO;
    


    /**
     * 查询app详细信息
     *
     * @return 返回app列表
     */
    @Override
    public List<ESUser> listESUsers(List<Integer> projectIds) {
        return         ConvertUtil.list2List(esUserDAO.listByProjectIds(projectIds), ESUser.class);
    }

  



    /**
     * 新建APP
     *
     * @param esUserDTO dto
     * @param operator  操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Result, ESUserPO> registerESUser(ESUserDTO esUserDTO,
                                                   String operator) {
       
        Result<Void> checkResult = validateESUser(esUserDTO, ADD);
        
        if (checkResult.failed()) {
            LOGGER.warn("class=ESUserManagerImpl||method=addApp||fail msg={}", checkResult.getMessage());
            return Tuple.of(checkResult,null);
        }
        initParam(esUserDTO);
        ESUserPO param = obj2Obj(esUserDTO, ESUserPO.class);
        final int countByProjectId = esUserDAO.countByProjectId(esUserDTO.getProjectId());
        //如果项目中已经存在es user，那么setDefaultDisplay为false
        if (countByProjectId == 0) {
            //新创建的项目会默认创建一个es user ，作为当前项目的默认es user
            param.setDefaultDisplay(true);
        } else {
            param.setDefaultDisplay(false);
        }
        
        boolean succ = (esUserDAO.insert(param) == 1);
       

        return Tuple.of(Result.build(succ, param.getId()),param);
    }



    /**
     * 编辑APP
     *
     * @param esUserDTO dto
     * @return 成功 true  失败 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Result<Void>/*更新的状态*/, ESUserPO/*更新之后的的ESUserPO*/> editUser(ESUserDTO esUserDTO) {
        
        final ESUserPO param = obj2Obj(esUserDTO, ESUserPO.class);
      
        return Tuple.of(Result.build((esUserDAO.update(param) == 1)), param);
    
    }

    /**
     * 删除APP
     *
     * @param esUser
     * @return 成功 true  失败 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Result<Void>, ESUserPO> deleteESUserById(int esUser) {
        
        ESUserPO oldPO = esUserDAO.getByESUser(esUser);
        boolean succ = esUserDAO.delete(esUser) == 1;
      

        return Tuple.of(Result.build(succ),oldPO);
    }
     /**
      * @param projectId
      * @return
      */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Tuple2<Result<Void>, List<ESUserPO>> deleteByESUsers(int projectId) {
        final List<ESUserPO> esUserPOS = esUserDAO.listByProjectId(projectId);
        final int deleteByProjectId = esUserDAO.deleteByProjectId(projectId);
        return Tuple.of(Result.build(deleteByProjectId==esUserPOS.size()), esUserPOS);
    }
    
    /**
     * 获取项目下es user 个数
     *
     * @param projectId 项目id
     * @return int
     */
    @Override
    public int countByProjectId(int projectId) {
        return esUserDAO.countByProjectId(projectId);
    }
    
   



   





    /**
     * 指定id查询
     *
     * @param esUser esuer
     * @return app  如果不存在返回null
     */
    @Override
    public ESUser getEsUserById(Integer esUser) {
        return obj2Obj(esUserDAO.getByESUser(esUser), ESUser.class);
    }

  


    /**
     * 校验验证码
     *
     * @param esUserName     esuser
     * @param verifyCode 验证码
     * @return result
     */
    @Override
    public Result<Void> verifyAppCode(Integer esUserName, String verifyCode) {
        final ESUserPO esUser = esUserDAO.getByESUser(esUserName);
    
        if (esUser == null) {
            return Result.buildNotExist(ES_USER_NOT_EXIST);
        }

        if (StringUtils.isBlank(verifyCode) || !esUser.getVerifyCode().equals(verifyCode)) {
            return Result.buildParamIllegal("校验码错误");
        }

        return Result.buildSucc();
    }



    
    
    /**
     * 验证APP参数是否合法
     *
     * @param appDTO    dto
     * @param operation 是否校验null参数;  新建的时候需要校验,编辑的时候不需要校验
     * @return 参数合法返回
     */
    @Override
    public Result<Void> validateESUser(ESUserDTO appDTO, OperationEnum operation) {
        if (AriusObjUtils.isNull(appDTO)) {
            return Result.buildParamIllegal("应用信息为空");
        }
        // if (appDTO.getMemo() == null) {
        //    return Result.buildParamIllegal("备注为空");
        //}
       if (Objects.isNull(appDTO.getProjectId())) {
            return Result.buildParamIllegal("项目id为空");
        }
    
        ESUserPO oldESUser = null;
        if (Objects.nonNull(appDTO.getId())) {
            oldESUser = esUserDAO.getByESUser(appDTO.getId());
        }

        if (ADD.equals(operation)) {
            if (Objects.nonNull(oldESUser)&&!appDTO.getProjectId().equals(oldESUser.getProjectId())) {
                return Result.buildParamIllegal(String.format("es user [%s] 已存在", appDTO.getId()));
            }
        } else if (EDIT.equals(operation)) {
            if (AriusObjUtils.isNull(appDTO.getId())||AriusObjUtils.isNull(oldESUser)) {
                return Result.buildNotExist(ES_USER_NOT_EXIST);
            }
           
            //判断当前es user 在同一个项目中
            if (Objects.nonNull(oldESUser) && !Objects.equals(oldESUser.getProjectId(), appDTO.getProjectId())) {
                return Result.buildParamIllegal(
                        String.format("es user 已经存在在项目[%s],不能为项目[%s]创建,请重新提交es user", oldESUser.getProjectId(),
                                appDTO.getProjectId()));
            }
        }
    
        if (appDTO.getIsRoot() == null || !AdminConstant.yesOrNo(appDTO.getIsRoot())) {
            return Result.buildParamIllegal("超管标记非法");
        }
        AppSearchTypeEnum searchTypeEnum = AppSearchTypeEnum.valueOf(appDTO.getSearchType());
        if (searchTypeEnum.equals(AppSearchTypeEnum.UNKNOWN)) {
            return Result.buildParamIllegal("查询模式非法");
        }
        final int countByProjectIdAndSearchType = esUserDAO.countByProjectIdAndSearchType(appDTO.getSearchType(),
                appDTO.getProjectId());
        if (StringUtils.isBlank(appDTO.getVerifyCode())) {
            return Result.buildParamIllegal("校验码不能为空");
        }
        if (countByProjectIdAndSearchType > 1) {
            return Result.buildParamIllegal("当前项目已经存在相同模式的es user");
        }
        
        
        return Result.buildSucc();
    }
    
    /**
     * 查询项目下可以免密登录的es user
     *
     * @param projectId projectId
     * @return appList
     */
    @Override
    public List<ESUser> getProjectWithoutCodeApps(Integer projectId) {
    
        return listESUsers(Collections.singletonList(projectId));
    
    }
    
   
    
    /**
     * 获取项目id通过搜索类型
     *
     * @param searchType 搜索类型
     * @return {@code List<Integer>}
     */
    @Override
    public List<Integer> getProjectIdBySearchType(Integer searchType) {
        return esUserDAO.getProjectIdBySearchType(searchType);
    }
    
    /**
     *
     * @param projectId
     * @return
     */
    @Override
    public boolean checkDefaultESUserByProject(Integer projectId) {
        return esUserDAO.countDefaultESUserByProject(projectId)==1;
    }
    
    /**
     *
     * @param projectId
     * @return
     */
    @Override
    public ESUser getDefaultESUserByProject(Integer projectId) {
        return esUserDAO.getDefaultESUserByProject(projectId);
    }
    
    /**************************************** private method ****************************************************/
    

    private void initParam(ESUserDTO esUser) {
        // 默认不是root用户
        if (esUser.getIsRoot() == null) {
            esUser.setIsRoot(AdminConstant.NO);
        }

        if (StringUtils.isBlank(esUser.getDataCenter())) {
            esUser.setDataCenter(EnvUtil.getDC().getCode());
        }

        // 默认cluster=""
        if (esUser.getCluster() == null) {
            esUser.setCluster("");
        }
        
        // 默认索引模式
        if (esUser.getSearchType() == null) {
            esUser.setSearchType(AppSearchTypeEnum.TEMPLATE.getCode());
        }

        // 生成默认的校验码
        if (StringUtils.isBlank(esUser.getVerifyCode())) {
            esUser.setVerifyCode(VerifyCodeFactory.get(VERIFY_CODE_LENGTH));
        }

        // 设置默认查询限流值
        if (esUser.getQueryThreshold() == null) {
            esUser.setQueryThreshold(APP_QUERY_THRESHOLD_DEFAULT);
        }
    }






  
   
}