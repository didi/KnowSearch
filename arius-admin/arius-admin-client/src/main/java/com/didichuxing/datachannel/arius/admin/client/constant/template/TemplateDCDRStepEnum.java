package com.didichuxing.datachannel.arius.admin.client.constant.template;

public enum TemplateDCDRStepEnum {
                                  /**第一步：停止主索引写入*/
                                  STEP_1(1, "停止主索引写入%s"),

                                  STEP_2(2, "确保主从索引数据同步%s"),

                                  STEP_3(3, "删除源dcdr模板和索引链路%s"),

                                  STEP_4(4, "拷贝主模板的mapping信息到从模板%s"),

                                  STEP_5(5, "关闭从索引dcdr索引开关并打开主索引dcdr索引开关%s"),

                                  STEP_6(6, "停止从索引写入%s"),

                                  STEP_7(7, "创建新的dcdr链路%s"),

                                  STEP_8(8, "恢复主从索引实时写入%s"),

                                  STEP_9(9, "主从模板角色切换%s"),

                                  UNKNOWN(-1, "unknown");

    TemplateDCDRStepEnum(int step, String value) {
        this.step = step;
        this.value = value;
    }

    private int    step;

    private String value;

    public int getStep() {
        return step;
    }

    public String getValue() {
        return value;
    }

    public static TemplateDCDRStepEnum valueOfStep(int step) {

        for (TemplateDCDRStepEnum stepEnum : TemplateDCDRStepEnum.values()) {
            if (step == stepEnum.getStep()) {
                return stepEnum;
            }
        }
        return TemplateDCDRStepEnum.UNKNOWN;
    }
}
