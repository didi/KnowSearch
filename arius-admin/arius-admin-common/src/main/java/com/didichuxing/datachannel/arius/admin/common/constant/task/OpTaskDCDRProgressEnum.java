package com.didichuxing.datachannel.arius.admin.common.constant.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum OpTaskDCDRProgressEnum {
                                      /**创建DCDR链路*/
                                      STEP_0(0, "创建DCDR链路"),

                                      STEP_1(1, "停止主索引写入"),

                                      STEP_2(2, "确保主从索引数据同步"),

                                      STEP_3(3, "删除源dcdr模板和索引链路"),

                                      STEP_4(4, "拷贝主模板的mapping信息到从模板"),

                                      STEP_5(5, "关闭从索引dcdr索引开关并打开主索引dcdr索引开关"),

                                      STEP_6(6, "停止从索引写入"),

                                      STEP_7(7, "创建新的dcdr链路"),

                                      STEP_8(8, "恢复主从索引实时写入"),

                                      STEP_9(9, "主从模板角色切换"),

                                      UNKNOWN(-1, "unknown");

    OpTaskDCDRProgressEnum(Integer progress, String value) {
        this.progress = progress;
        this.value = value;
    }

    private final Integer progress;

    private final String  value;

    public Integer getProgress() {
        return progress;
    }

    public String getValue() {
        return value;
    }

    public static List<OpTaskDCDRProgressEnum> listAll() {
        return new ArrayList<>(Arrays.asList(OpTaskDCDRProgressEnum.values()));
    }
}
