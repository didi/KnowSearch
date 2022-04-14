package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElasticCloudUserLog {
    private String apiVersion ;

    private List<ElasticCloudUserLogItem> items;

    public String convert2FormattedString() {
        if (items == null || items.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (ElasticCloudUserLogItem item: items) {
            sb.append(item.convert2FormattedString()).append("\n");
        }
        return sb.toString();
    }
}
