package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/5/7
 */
@Data
public class AppCreateContent extends BaseContent {

    /**
     * 名字
     */
    private String name;

    /**
     * 部门id
     */
    private String departmentId;

    /**
     * 部门名称
     */
    private String department;

    /**
     * 责任人
     */
    private String responsible;

    /**
     * 备注
     */
    private String memo;

}
