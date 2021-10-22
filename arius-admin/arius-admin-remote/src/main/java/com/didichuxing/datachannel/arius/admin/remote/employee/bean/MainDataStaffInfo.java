package com.didichuxing.datachannel.arius.admin.remote.employee.bean;

import lombok.Data;

@Data
public class MainDataStaffInfo {
    /**
     * 域账号
     */
    private String ldap;
    /**
     * 员工姓名
     */
    private String name;
    /**
     * 员工工号
     */
    private String empId;
    /**
     * 员工所在的一级部门
     */
    private String deptCode1;
    /**
     * 员工所在的一级部门名
     */
    private String deptDescr1;
    /**
     * 员工所在的二级部门
     */
    private String deptCode2;
    /**
     * 员工所在的三级部门
     */
    private String deptCode3;
    /**
     * 员工状态, "A":在职, "I":离职, "P":将离职
     */
    private String emplStatus;
    /**
     * 员工状态, "A":在职, "I":离职
     */
    private String hrStatus;
    /**
     * 员工对应的直接leader的邮箱
     */
    private String managerEmail;
    /**
     * 员工对应的直接leader的工号
     */
    private String managerId;
    /**
     * 员工对应的直接leader的名字
     */
    private String managerName;
}
