package com.didichuxing.datachannel.arius.admin.remote.employee.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EpHrEmplInfoData {
    public static String USER_ON_LINE  = "A";
    public static String USER_OFF_LINE = "I";

    /**
     * 工号
     */
    private String       empId;

    /**
     * 邮箱地址
     */
    private String       emailAddr;

    /**
     * 员工在职状态  A 在职   I 离职
     */
    private String       hrStatus;

    /**
     * 一级部门编码
     */
    private String       deptId1;

    /**
     * 一级部门名称
     */
    private String       deptDescr1;

    /**
     * 二级部门编码
     */
    private String       deptId2;

    /**
     * 二级部门名称
     */
    private String       deptDescr2;

    /**
     * 部门编码
     */
    private String       deptid;

    /**
     * 直属部门名称
     */
    private String       deptName;

    /**
     * 主管工号
     */
    private String       managerId;

    /**
     * 主管姓名
     */
    private String       managerName;

    /**
     * 主管域账号
     */
    private String       managerLdap;
}
