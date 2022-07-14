package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.TypeDefine;

/**
 * Created by linyunan on 3/22/22
 */

public class MappingConfigUtil {
    private MappingConfigUtil() {
    }

    public static Integer countMappingFieldNum(MappingConfig mapping) {
        if (null == mapping) {
            return 0;
        }

        Map<String/*typeName*/, Map<String/*field*/, TypeDefine>> typeFieldTypeMap = mapping.getTypeDefines();
        if (MapUtils.isEmpty(typeFieldTypeMap)) {
            return 0;
        }

        int count = 0;
        for (Map.Entry<String/*typeName*/, Map<String/*field*/, TypeDefine>> entry : typeFieldTypeMap.entrySet()) {
            count += entry.getValue().keySet().size();
        }
        return count;
    }

}
