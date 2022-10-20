package com.didichuxing.datachannel.arius.admin.common.constant.operaterecord;

/**
 * 操作枚举
 * <p>
 *
 * @author d06679
 * @date 2017/7/14
 */
public enum OperationEnum {
                           /**
                            * 新增
                            */
                           ADD(1, "新增"),

                           DELETE(2, "删除"),

                           EDIT(3, "修改"),

                           ENABLE(4, "启用"),

                           DISABLE(5, "禁用"),

                           EXE(6, "执行"),

                           EDIT_LABELS(7, "修改标签"),

                           PRE_CREATE_INDEX(8, "预创建索引"),

                           WO_SUBMIT(9, "提交"),

                           WO_ARIUS_REVIEW(10, "Arius审核"),

                           WO_DATA_OWNER_REVIEW(11, "第三方审核"),

                           WO_ARIUS_REJECT(12, "Arius驳回"),

                           WO_DATA_OWNER_REJECT(13, "第三方驳回"),

                           WO_CANCEL(14, "撤销"),

                           WO_EXECUTE(15, "执行"),

                           WO_EDIT(16, "提交"),

                           SWITCH(17, "使能"),

                           UPGRADE(18, "升版本"),

                           COPY(19, "复制"),

                           SWITCH_MASTER_SLAVE(20, "主从切换"),

                           CREATE_DCDR(21, "创建dcdr链路"),

                           DELETE_DCDR(22, "删除dcdr链路"),

                           SENSE_GET(30, "sense查询"),

                           SENSE_DEL(31, "sense删除"),

                           SENSE_PUT(32, "sense更新"),

                           SENSE_OTHER(39, "sense其他"),

                           DELETE_INDEX(40, "删除索引"),

                           CAPACITY_PAN_INIT_REGION(41, "region初始化"),

                           BATCH_CHANGE_TEMPLATE_HOT_DAYS(42, "批量修改模板hotDay"),

                           EDIT_TEMPLATE_MAPPING(43, "修改模板mapping"),

                           EDIT_TEMPLATE_ALIASES(44, "修改模板aliases"),

                           CONFIG_ROLL_BACK(45, "回滚"),

                           REGION_BIND(46, "region绑定"),

                           REGION_UNBIND(47, "region解绑"),

                           CLUSTER_CONFIG(48, "修复/新增配置"),

                           CHECK(49, "校验"), BLOCK_READ(50, "禁读"),

                           BLOCK_WRITE(51, "禁写"),

                           CLOSE_INDEX(52, "关闭索引"),

                           OPEN_INDEX(53, "开启索引"),
    ADD_BIND_MULTIPLE_PROJECT(54, "绑定逻辑集群"),


                           UNKNOWN(-1, "unknown");

    OperationEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int    code;

    private String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static OperationEnum valueOf(Integer code) {
        if (code == null) {
            return OperationEnum.UNKNOWN;
        }
        for (OperationEnum state : OperationEnum.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }

        return OperationEnum.UNKNOWN;
    }

    public static boolean validate(Integer code) {
        if (code == null) {
            return false;
        }
        for (OperationEnum state : OperationEnum.values()) {
            if (state.getCode() == code) {
                return true;
            }
        }

        return false;
    }
}