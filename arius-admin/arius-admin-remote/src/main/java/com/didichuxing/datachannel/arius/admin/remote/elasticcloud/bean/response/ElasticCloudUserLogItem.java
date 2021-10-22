package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElasticCloudUserLogItem {
    private Integer id;

    private String categoryType;

    private Integer categoryId;

    private String message;

    private String logLevel;

    private Integer taskId;

    private String opUser;

    private String createTime;

    public String convert2FormattedString() {
        return new StringBuffer().append(this.createTime)
                .append(" [")
                .append(logLevel)
                .append("] [taskID:")
                .append(taskId)
                .append("] ")
                .append(message).toString();
    }
}
