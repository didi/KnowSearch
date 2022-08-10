package com.didiglobal.logi.op.manager.domain.packages.entity;

import com.didiglobal.logi.op.manager.domain.packages.entity.value.PackageGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.PackageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author didi
 * @date 2022-07-11 2:08 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Package {
    /**
     * 安装包id
     */
    private Integer id;
    /**
     * 安装包名字
     */
    private String name;
    /**
     * 地址
     */
    private String url;
    /**
     * 版本
     */
    private String version;
    /**
     * 描述
     */
    private String describe;
    /**
     * 类型，1是配置独立，0是配置依赖
     */
    private Integer type;
    /**
     * 脚本id
     */
    private Integer scriptId;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;
    /**
     * 传输文件
     */
    private MultipartFile uploadFile;


    /**
     * 关联的默认安装包分组配置
     */
    private List<PackageGroupConfig> groupConfigList;


    public Package create() {
        if (null == type) {
            type = PackageTypeEnum.CONFIG_INDEPENDENT.getType();
        }
        createTime = new Timestamp(System.currentTimeMillis());
        updateTime = new Timestamp(System.currentTimeMillis());
        if (null != groupConfigList) {
            groupConfigList.forEach(config -> config.create());
        }
        return this;
    }


    public Result<Void> checkCreateParam() {
        if (name.isEmpty()) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "name缺失");
        }

        if (version.isEmpty()) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "version缺失");
        }

        if (null == scriptId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "未绑定脚本");
        }

        if (uploadFile.isEmpty()) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "安装包内容缺失");
        }
        return Result.success();
    }
}
