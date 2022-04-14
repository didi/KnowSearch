package com.didichuxing.datachannel.arius.admin.core.service.extend.department.impl;

import com.didichuxing.datachannel.arius.admin.remote.storage.content.FileStorageTypeEnum;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.extend.department.DepartmentService;
import com.didichuxing.datachannel.arius.admin.remote.department.DepartmentHandle;
import com.didichuxing.datachannel.arius.admin.remote.department.content.DepartmentTypeEnum;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

import javax.annotation.PostConstruct;

/**
 * @author linyunan
 * @date 2021-04-26
 */
@Service
@NoArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private static final ILog  LOGGER             = LogFactory.getLog(DepartmentServiceImpl.class);

    @Autowired
    private HandleFactory      handleFactory;

    @Value("${extend.department}")
    private String departmentType;

    @PostConstruct
    public void departmentTypeCheck() {
        DepartmentTypeEnum departmentTypeEnum = DepartmentTypeEnum.valueOfType(departmentType);
        if (departmentTypeEnum.getCode().equals(FileStorageTypeEnum.UNKNOWN.getCode())) {
            LOGGER.info("class=DepartmentServiceImpl||method=departmentTypeCheck||departmentType={}", departmentTypeEnum);
        }
    }

    @Override
    public Result<String> listDepartmentsByType() {
        if (AriusObjUtils.isNull(departmentType)) {
            return Result.build(Boolean.TRUE, DepartmentTypeEnum.DEFAULT.getType());
        }

        if (DepartmentTypeEnum.valueOfType(departmentType).getCode() == -1) {
            return Result.buildFail(String.format("获取 %s 类型出错", departmentType));
        }

        LOGGER.info("class=DepartmentServiceImpl||method=listDepartmentsByType||departmentType={}",
            departmentType);

        return ((DepartmentHandle) handleFactory.getByHandlerNamePer(departmentType)).listDepartment();
    }
}
