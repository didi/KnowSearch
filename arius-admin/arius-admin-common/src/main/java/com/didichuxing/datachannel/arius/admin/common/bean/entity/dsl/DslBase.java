package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/6/21 下午2:47
 * @Modified By
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DslBase {

    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用账号", example = "1")
    private Integer projectId;

    /**
     * 查询模板的md5值
     */
    @ApiModelProperty(value = "查询模板MD5", example = "V2_EA317B2029682DB83A191CBD797A66FE")
    private String dslTemplateMd5;

    /**
     * 获取索引主键
     *
     * @return
     */
    @JSONField(serialize = false)
    public String getAppidDslTemplateMd5() {
        return String.format("%d_%s", this.projectId, this.dslTemplateMd5);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}