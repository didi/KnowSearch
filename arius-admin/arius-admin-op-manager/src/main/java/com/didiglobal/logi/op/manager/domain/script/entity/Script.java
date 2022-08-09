package com.didiglobal.logi.op.manager.domain.script.entity;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-04 6:59 下午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Script {
    /**
     * 脚本id
     */
    private Integer id;
    /**
     * 脚本名
     */
    private String name;
    /**
     * 模板id
     */
    private String templateId;
    /**
     * 内容地址
     */
    private String contentUrl;
    /**
     * 描述
     */
    private String describe;
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

    public Script create() {
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
        return this;
    }

    public Script update() {
        this.updateTime = new Timestamp(System.currentTimeMillis());
        return this;
    }

    public Result<Void> checkCreateParam() {
        if (name.isEmpty()) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "name缺失");
        }

        if (uploadFile.isEmpty()) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "脚本内容缺失");
        }
        return Result.success();
    }

    public Result<Void> checkUpdateParam() {
        if (null == id) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "id缺失");
        }

        if (uploadFile.isEmpty() && null == describe) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "请指定要修改的值（脚本或者描述）");
        }

        return Result.success();
    }

}
