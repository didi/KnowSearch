package com.didichuxing.datachannel.arius.admin.common.util;

import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;

public class MetadataControlUtils {
    private MetadataControlUtils(){
    }

    /**
     * false为不展示元数据集群信息，true为展示
     * @param showMetadata 是否展示元数据标识
     * @param projectId
     * @return
     */
    public static Boolean showMetadataInfo(Boolean showMetadata, Integer projectId) {
        return showMetadata && AuthConstant.SUPER_PROJECT_ID.equals(projectId);
    }
}
