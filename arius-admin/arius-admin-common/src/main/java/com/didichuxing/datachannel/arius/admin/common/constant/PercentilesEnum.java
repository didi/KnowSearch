package com.didichuxing.datachannel.arius.admin.common.constant;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * Created by linyunan on 11/17/21
 */
public enum PercentilesEnum {
    ST99("99.0"),
    ST95("95.0"),
    ST75("75.0"),
    ST55("55.0"),
    ST45("45.0"),
    ST25("25.0"),
    ST5("5.0"),
    ST1("1.0"),
    AVG("avg");

    PercentilesEnum(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

    public static List<PercentilesEnum> list(){
        List<PercentilesEnum> percentilesEnumList= Lists.newArrayList();
        percentilesEnumList.addAll(Arrays.asList(PercentilesEnum.values()));
        return percentilesEnumList;
    }

    public static List<String> listAllType(){
        List<String> percentilesTypeList= Lists.newArrayList();
        for (PercentilesEnum value : PercentilesEnum.values()) {
            percentilesTypeList.add(value.getType());
        }
        return percentilesTypeList;
    }

    public static List<String> listSpecialType() {
        return Lists.newArrayList(ST1.getType(), ST5.getType(), ST25.getType(), ST45.getType());
    }

    public static List<String> listUsefulType() {
        List<String> specialTypeList = listSpecialType();
        List<String> percentilesTypeList = Lists.newArrayList();
        for (PercentilesEnum value : PercentilesEnum.values()) {
            if (specialTypeList.contains(value.getType())) {
                continue;
            }
            percentilesTypeList.add(value.getType());
        }
        return percentilesTypeList;
    }
}
