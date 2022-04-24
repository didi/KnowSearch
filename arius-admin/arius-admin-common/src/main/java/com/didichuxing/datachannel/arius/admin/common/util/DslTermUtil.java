package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.List;

/**
 * Created by linyunan on 3/23/22
 */

public class DslTermUtil {
    private DslTermUtil(){}

    public static String buildTermsDslByIndexList(List<String> indexList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indexList.size(); i++) {
            if (i == 0) { sb.append("[");}
            sb.append("\"").append(indexList.get(i)).append("\"");
            if (i != indexList.size() - 1) { sb.append(",");}
            if (i == indexList.size() - 1) { sb.append("]");}
        }
        return sb.toString();
    }

}
