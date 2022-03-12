package com.didichuxing.datachannel.arius.admin.common.util;

import org.apache.commons.lang3.StringUtils;

public class ESVersionUtil {

    private ESVersionUtil(){}

    private final static int MIN_VERSION_STAGE = 3;

    /**
     * 判断是不是标准的es版本号，如：6.6.1.1000 或者 6.6.1
     * @param version
     * @return
     */
    public static boolean isValid(String version){
        if(StringUtils.isBlank(version)){return false;}

        String[] vers = version.split("\\.");
        if(null == vers || vers.length < MIN_VERSION_STAGE){return false;}

        for(String ver : vers){
            if(!ver.chars().allMatch(Character::isDigit)){
                return false;
            }
        }

        return true;
    }

    /**
     * 判断下version1是不是比version2的版本高
     * @param version1
     * @param version2
     * @return
     */
    public static boolean isHigher(String version1, String version2) {
        if (!isValid(version1) || !isValid(version2)) {
            return false;
        }

        String[] vers1 = version1.split("\\.");
        String[] vers2 = version2.split("\\.");

        for (int i = 0; i < MIN_VERSION_STAGE; i++) {
            if (Integer.parseInt(vers1[i]) > Integer.parseInt(vers2[i])) {
                return true;
            }
        }

        return false;
    }
}
