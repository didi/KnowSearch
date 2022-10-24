package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

import java.util.stream.Stream;

/**
 * @Authoer: zyl
 * @Date: 2022/10/24
 * @Version: 1.0
 */
public enum RegionDivideMethodEnum {
    RACK("rack");

    private String divideMethod;

    RegionDivideMethodEnum(String divideMethod){
        this.divideMethod = divideMethod;
    }

    public String getDivideMethod() {
        return divideMethod;
    }

    public void setDivideMethod(String divideMethod) {
        this.divideMethod = divideMethod;
    }


    public static Stream<RegionDivideMethodEnum> stream() {
        return Stream.of(RegionDivideMethodEnum.values());
    }
}
