package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/10/24 下午3:46
 * @Modified By
 *
 * 异常查询一行记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDslEmailLine {
    /**
     * 日期
     */
    private String date;
    /**
     * 异常名称
     */
    private String exceptionName;
    /**
     * 次数
     */
    private Long   count;
    /**
     * 索引名称
     */
    private String indices;
    /**
     * 查询语句
     */
    private String dsl;

}
